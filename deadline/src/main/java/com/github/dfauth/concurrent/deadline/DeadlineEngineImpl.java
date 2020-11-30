package com.github.dfauth.concurrent.deadline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DeadlineEngineImpl implements DeadlineEngine {

    private static final Logger logger = LoggerFactory.getLogger(DeadlineEngineImpl.class);

    private NavigableMap<Long,Collection<Long>> requestIdsByDeadline = new ConcurrentSkipListMap<>();
    private Map<Long,Long> deadlinesByRequestId = new ConcurrentHashMap<>();
    private AtomicLong counter = new AtomicLong(0);

    @Override
    public long schedule(long deadlineMs) {
        long requestId = counter.getAndIncrement();
        deadlinesByRequestId.put(requestId, deadlineMs);
        Collection<Long> ids = requestIdsByDeadline.computeIfAbsent(deadlineMs, k -> new ConcurrentSkipListSet<>());
        ids.add(requestId);
        return requestId;
    }

    @Override
    public boolean cancel(long requestId) {
        return Optional.ofNullable(deadlinesByRequestId.remove(requestId)).map(deadline ->
            requestIdsByDeadline.getOrDefault(deadline, Collections.emptyList()).remove(requestId)
        ).orElse(false);
    }

    @Override
    public int poll(long deadlineMs, Consumer<Long> handler, int maxPoll) {
        AtomicInteger cnt = new AtomicInteger(0);
        return requestIdsByDeadline.headMap(deadlineMs)
                .values()
                .stream()
                .flatMap(v -> v.stream())
                .filter(v -> cnt.getAndIncrement() < maxPoll)
                .map(v -> {
                    if(cancel(v)) {
                        try {
                            handler.accept(v);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            return 1;
                        }
                    } else {
                        return 0;
                    }
                })
                .reduce(0,
                        (acc,v) -> acc+v);
    }

    @Override
    public int size() {
        return deadlinesByRequestId.size();
    }
}

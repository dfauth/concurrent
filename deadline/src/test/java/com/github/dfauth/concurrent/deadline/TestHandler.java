package com.github.dfauth.concurrent.deadline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TestHandler implements Consumer<Long> {

    private static final Logger logger = LoggerFactory.getLogger(TestHandler.class);

    private Map<Long, Integer> requestIds = new ConcurrentHashMap<>();

    @Override
    public void accept(Long id) {
        logger.info("executing requestId {}", id);
        requestIds.compute(id, (k,v) -> v == null ? 1 : v+1);
    }

    public boolean wasTriggered() {
        return ! requestIds.isEmpty();
    }

    public boolean wasTriggeredWith(Long requestId) {
        return requestIds.containsKey(requestId);
    }

    public boolean wasTriggeredAndReset() {
        try {
            return wasTriggered();
        } finally {
            requestIds.clear();
        }
    }

    public boolean wasTriggeredWithAndReset(Long requestId) {
        try {
            return wasTriggeredWith(requestId);
        } finally {
            requestIds.clear();
        }
    }

    public Collection<Long> triggeredRequestIds() {
        return requestIds.keySet();
    }

    public int executionCount(long id) {
        return requestIds.getOrDefault(id, 0);
    }
}

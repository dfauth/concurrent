package com.github.dfauth.concurrent.cache;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.concurrent.cache.TryCatch.executors;
import static java.lang.Math.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    @Test
    public void testPerformance() throws InterruptedException, ExecutionException, TimeoutException {
        int trials = 100;
        int executors = 30;
        { // warm up
            CompletableFuture<Double> f0 = doit(trials, executors, f -> new ConcurrentHashMapCacheImpl<>(f));
            CompletableFuture<Double> f1 = doit(trials, executors, f -> new SynchronizedCacheImpl<>(f));
            CompletableFuture<Double> f2 = doit(trials, executors, f -> new BucketCacheImpl(f));
            CompletableFuture.allOf(f0,f1,f2).get(10, TimeUnit.SECONDS);
        }

        CompletableFuture<Double> f0 = doit(trials, executors, f -> new ConcurrentHashMapCacheImpl<>(f));
        CompletableFuture<Double> f1 = doit(trials, executors, f -> new SynchronizedCacheImpl<>(f));
        CompletableFuture<Double> f2 = doit(trials, executors, f -> new BucketCacheImpl(f, trials));
        f0.thenAccept(elapsed -> {
            logger.info("ConcurrentHashMapCacheImpl completed in {} msec",elapsed);
        });
        f1.thenAccept(elapsed -> {
            logger.info("SynchronizedCacheImpl completed in {} msec",elapsed);
        });
        f2.thenAccept(elapsed -> {
            logger.info("BucketCacheImpl completed in {} msec",elapsed);
        });
        CompletableFuture.allOf(f0,f1,f2).get(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Double> doit(int trials, int executors, Function<Function<Integer, Integer>, Cache<Integer,Integer>> fn) throws InterruptedException, ExecutionException, TimeoutException {
        AtomicInteger executionCounter = new AtomicInteger();
        Cache<Integer, Integer> cache = fn.apply(k -> {
            executionCounter.incrementAndGet();
            return k;
        });

        CompletableFuture<Double> f = executors(executors).execute(() -> {
            for (int i = 0; i < trials; i++) {
                int k = (int)(random()*trials);
//                int k = i;
                Integer v = cache.get(k);
                assertEquals(k, v.intValue());
            }
        });
        return f;
    }


}

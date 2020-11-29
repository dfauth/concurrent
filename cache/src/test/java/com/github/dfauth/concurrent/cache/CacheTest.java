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

import static com.github.dfauth.concurrent.cache.TryCatch.executors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public abstract class CacheTest {

    private static final Logger logger = LoggerFactory.getLogger(CacheTest.class);

    protected Function<Integer, String> testFn = i -> UUID.randomUUID().toString();

    protected abstract <K,V> Cache<K,V> newCache(Function<K,V> f);

    @Test
    public void testNonNullFunction() {
        try {
            newCache(null);
            fail("Null function not supported");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testNull() {
        Cache<Integer, String> cache = newCache(testFn);
        Integer k = null;
        String v = cache.get(k);
        assertEquals(v, cache.get(k));
    }

    @Test
    public void testCalledOnce() {
        AtomicInteger executionCounter = new AtomicInteger();
        Cache<Integer, String> cache = newCache(k -> {
            executionCounter.incrementAndGet();
            return UUID.randomUUID().toString();
        });
        int k = 0;
        String v = cache.get(k);
        assertEquals(cache.get(k), v);
        assertEquals(1, executionCounter.get());
    }

    @Test
    public void testIt() {
        Cache<Integer, String> cache = newCache(testFn);
        int k = 0;
        String v = cache.get(k);
        assertEquals(v, cache.get(k));
    }

    @Test
    public void testMultiThreaded() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicInteger executionCounter = new AtomicInteger();
        Cache<Integer, Integer> cache = newCache(k -> {
            executionCounter.incrementAndGet();
            return k;
        });

        int trials = 100;
        CompletableFuture<Double> f = executors(10).execute(() -> {
            for (int i = 0; i < trials; i++) {
                int k = i;
                Integer v = cache.get(k);
                assertEquals(k, v.intValue());
            }
        });
        Double elapsed = f.get(10, TimeUnit.SECONDS);
        assertEquals(trials, executionCounter.get());
        logger.info("cache implementation {} executed in {} msec", cache, elapsed);
    }


}

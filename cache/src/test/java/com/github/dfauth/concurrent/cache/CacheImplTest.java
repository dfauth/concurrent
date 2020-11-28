package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static org.junit.Assert.fail;

public class CacheImplTest extends CacheTest {

    private static final Logger logger = LoggerFactory.getLogger(CacheImplTest.class);

    @Override
    protected <K, V> Cache<K, V> newCache(Function<K, V> f) {
        return new CacheImpl<>(f);
    }

    @Override
    public void testNull() {
        try {
            Cache<Integer, String> cache = newCache(testFn);
            Integer k = null;
            String v = cache.get(k);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
}

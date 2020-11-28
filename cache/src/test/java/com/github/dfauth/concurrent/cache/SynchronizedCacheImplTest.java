package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class SynchronizedCacheImplTest extends CacheTest {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizedCacheImplTest.class);

    @Override
    protected <K, V> Cache<K, V> newCache(Function<K, V> f) {
        return new SynchronizedCacheImpl<>(f);
    }
}

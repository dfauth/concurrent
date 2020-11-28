package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConcurrentHashMapCacheImpl<K,V> implements Cache<K,V> {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentHashMapCacheImpl.class);

    private final Function<K, V> f;
    private final Map<K, V> nestedCache = new ConcurrentHashMap<>();

    public ConcurrentHashMapCacheImpl(Function<K,V> f) {
        Objects.requireNonNull(f);
        this.f = f;
    }

    @Override
    public V get(K key) {
        return nestedCache.computeIfAbsent(key, f);
    }
}

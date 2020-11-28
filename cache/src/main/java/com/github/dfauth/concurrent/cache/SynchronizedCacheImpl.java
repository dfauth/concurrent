package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class SynchronizedCacheImpl<K,V> implements Cache<K,V> {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizedCacheImpl.class);

    private final Function<K, V> f;
    private final Map<K, V> nestedCache = new HashMap<>();

    public SynchronizedCacheImpl(Function<K,V> f) {
        Objects.requireNonNull(f);
        this.f = f;
    }

    @Override
    public V get(K key) {
        V v;
        if ((v = nestedCache.get(key)) == null) {
            synchronized (nestedCache) {
                if ((v = nestedCache.get(key)) == null) {
                    if ((v = f.apply(key)) != null) {
                        nestedCache.put(key, v);
                    }
                }
            }
        }
        return v;
    }
}

package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.github.dfauth.concurrent.cache.Bucket.keyOf;
import static com.github.dfauth.concurrent.cache.Bucket.mapOfBucketsOfSize;

public class BucketCacheImpl<K,V> implements Cache<K,V> {

    private static final Logger logger = LoggerFactory.getLogger(BucketCacheImpl.class);

    private final Function<K, V> f;
    private int size = 100;
    private final Map<Integer, Bucket<K,V>> nestedCache = mapOfBucketsOfSize(size);

    public BucketCacheImpl(Function<K,V> f) {
        Objects.requireNonNull(f);
        this.f = f;
    }

    @Override
    public V get(K key) {
        V v;
        Bucket<K,V> bucket = nestedCache.get(keyOf(key, size));
        if ((v = bucket.get(key)) == null) {
            synchronized (bucket) {
                if ((v = bucket.get(key)) == null) {
                    if ((v = f.apply(key)) != null) {
                        bucket.put(key, v);
                    }
                }
            }
        }
        return v;
    }

}

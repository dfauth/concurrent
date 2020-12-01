package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.github.dfauth.concurrent.cache.Bucket.mapOfBucketsOfSize;

public class BucketCacheImpl<K,V> implements Cache<K,V> {

    private static final Logger logger = LoggerFactory.getLogger(BucketCacheImpl.class);

    private final Function<K, V> f;
    private final int size ;
    private final Map<Integer, Bucket<K,V>> nestedCache;

    public BucketCacheImpl(Function<K,V> f) {
        this(f, 100);
    }

    public BucketCacheImpl(Function<K,V> f, int size) {
        Objects.requireNonNull(f);
        this.f = f;
        this.size = size;
        this.nestedCache = mapOfBucketsOfSize(size);
    }

    @Override
    public V get(K key) {
        V v;
        Bucket<K,V> bucket = nestedCache.get(keyOf(key));
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

    private int keyOf(K k) {
        return k.hashCode()%size;
    }

}

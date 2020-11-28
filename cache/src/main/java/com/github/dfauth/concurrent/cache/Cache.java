package com.github.dfauth.concurrent.cache;

public interface Cache<K, V> {
    V get(K key);
}

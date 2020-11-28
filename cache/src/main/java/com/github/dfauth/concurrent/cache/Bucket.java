package com.github.dfauth.concurrent.cache;

import java.util.HashMap;
import java.util.Map;

public class Bucket<K,V> {

    private Map<K,V> map = new HashMap<>();

    public static <K,V> Map<Integer, Bucket<K,V>> mapOfBucketsOfSize(int n) {
        Map<Integer, Bucket<K,V>> tmp = new HashMap<>();
        for(int i=0; i<n; i++) {
            tmp.put(i,new Bucket());
        }
        return tmp;
    }

    public static <K> int keyOf(K k, int size) {
        return k.hashCode()%size;
    }

    public V get(K k) {
        return map.get(k);
    }

    public void put(K k,V v) {
        map.put(k,v);
    }
}

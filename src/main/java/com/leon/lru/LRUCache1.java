package com.leon.lru;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 采用LinkedHashMap实现LRU算法
 * 主要是设置accessOrder为true以及重写removeEldestEntry方法
 * 也可以采用委托来实现
 * @author leon
 * @since 2020/3/17 19:10
 */
public class LRUCache1<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_CACHE_SIZE;

    public LRUCache1(int cacheSize) {
        super((int) Math.ceil(cacheSize / 0.75) + 1, 0.75f, true);
        this.MAX_CACHE_SIZE = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > MAX_CACHE_SIZE;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : entrySet()) {
            sb.append(java.lang.String.format("%s:%s", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}

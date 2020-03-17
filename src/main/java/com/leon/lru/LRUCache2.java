package com.leon.lru;

import java.util.HashMap;

/**
 * 采用HashMap+链表实现
 *
 * @author leon
 * @since 2020/3/17 19:56
 */
public class LRUCache2<K, V> {
    // HashMap中的value对应的元素
    class Entry<K, V> {
        // 指向链表前一个
        public Entry<K, V> pre;
        // 指向链表后一个
        public Entry<K, V> next;
        public K key;
        public V value;
    }

    private final int MAX_CACHE_SIZE;
    private Entry<K, V> first;
    private Entry<K, V> last;
    private HashMap<K, Entry<K, V>> hashMap;

    public LRUCache2(int cacheSize) {
        this.MAX_CACHE_SIZE = cacheSize;
        hashMap = new HashMap<>();
    }

    public void put(K key, V value) {
        Entry entry = getEntry(key);
        if (null == entry) {
            if (hashMap.size() >= MAX_CACHE_SIZE) {
                hashMap.remove(last.key);
                removeLast();
            }
            entry = new Entry();
            entry.key = key;
        }
        entry.value = value;
        moveToFirst(entry);
        hashMap.put(key, entry);
    }

    public V get(K key) {
        Entry<K, V> entry = getEntry(key);
        if (null == entry) {
            return null;
        }
        moveToFirst(entry);
        return entry.value;
    }

    public void remove(K key) {
        Entry<K, V> entry = getEntry(key);
        if (null != entry) {
            if (entry.pre != null) {
                entry.pre.next = entry.next;
            }
            if (entry.next != null) {
                entry.next.pre = entry.pre;
            }
            if (entry == first) {
                first = entry.next;
            }
            if (entry == last) {
                last = entry.pre;
            }
        }
        hashMap.remove(key);
    }
    @Override
    public String toString() {
        if (null == first) {
            return null;
        }
        Entry<K, V> item = first;
        StringBuilder sb = new StringBuilder();
        for (; item != null; item = item.next) {
            sb.append(String.format("%s:%s", item.key, item.value)).append("  ");
        }
        return sb.toString();
    }

    private Entry<K, V> getEntry(K key) {
        return hashMap.get(key);
    }

    private void removeLast() {
        if (last != null) {
            last = last.pre;
            if (null == last) {
                first = null;
            } else {
                last.next = null;
            }
        }
    }

    private void moveToFirst(Entry entry) {
        if (entry == first) {
            return;
        }
        if (entry.pre != null) {
            entry.pre.next = entry.next;
        }
        if (entry.next != null) {
            entry.next.pre = entry.pre;
        }
        if (entry == last) {
            last = last.pre;
        }
        if (first == null || last == null) {
            first = last = entry;
            return;
        }
        entry.next = first;
        first.pre = entry;
        first = entry;
        entry.pre = null;
    }

    public static void main(String[] args) {
        System.out.println("=====LRU 链表实现======");
        LRUCache2<Integer, String> lru = new LRUCache2<>(5);
        lru.put(1, "11");
        lru.put(2, "11");
        lru.put(3, "11");
        lru.put(4, "11");
        lru.put(5, "11");
        System.out.println(lru.toString());
        lru.put(6, "66");
        lru.get(2);
        System.out.println(lru.toString());
    }
}

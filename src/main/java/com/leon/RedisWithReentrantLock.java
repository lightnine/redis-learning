package com.leon;

import com.leon.util.RedisConnectUtil;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.Map;

/**
 * redis实现可重入锁
 * 实现思路,采用ThreadLocal来记录当前锁的个数;
 * 注:没有考虑锁计数的过期时间
 *
 * @author leon
 * @since 2020/3/8 17:21
 */
public class RedisWithReentrantLock {
    private ThreadLocal<Map<String, Integer>> lockers = new ThreadLocal<>();
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;

    public RedisWithReentrantLock(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
        syncCommands = this.connection.sync();
    }

    /**
     * 上锁,锁超时时间5秒
     *
     * @param key key
     * @return 是否成功
     */
    private boolean _lock(String key) {
        SetArgs args = SetArgs.Builder.ex(5).nx();
        return syncCommands.set(key, "", args) != null;
    }

    private void _unlock(String key) {
        syncCommands.del(key);
    }

    private Map<String, Integer> currentLockers() {
        Map<String, Integer> refs = lockers.get();
        if (null != refs) {
            return refs;
        }
        lockers.set(new HashMap<>(2));
        return lockers.get();
    }

    public boolean lock(String key) {
        Map<String, Integer> refs = currentLockers();
        Integer refCnt = refs.get(key);
        // 如果已经上锁,则直接在锁个数上加 1
        if (null != refCnt) {
            refs.put(key, refCnt + 1);
            return true;
        }
        boolean ok = this._lock(key);
        if (!ok) {
            return false;
        }
        refs.put(key, 1);
        return true;
    }

    public boolean unlock(String key) {
        Map<String, Integer> refs = currentLockers();
        Integer refCnt = refs.get(key);
        if (null == refCnt) {
            return false;
        }
        refCnt -= 1;
        if (refCnt > 0) {
            refs.put(key, refCnt);
        } else {
            refs.remove(key);
            this._unlock(key);
        }
        return true;
    }

    public static void main(String[] args) {
        StatefulRedisConnection<String, String> connection = RedisConnectUtil.getConn();
        RedisWithReentrantLock lock = new RedisWithReentrantLock(connection);
        System.out.println(lock.lock("leon"));
        System.out.println(lock.lock("leon"));
        System.out.println(lock.unlock("leon"));
        System.out.println(lock.unlock("leon"));
    }
}

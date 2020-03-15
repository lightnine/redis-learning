package com.leon;

import com.leon.util.RedisConnectUtil;
import com.sun.javafx.image.IntPixelGetter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * 使用redis事务执行操作,并且采用watch乐观锁机制
 * @author leon
 * @since 2020/3/15 9:20
 */
public class TransactionDemo {
    public static void main(String[] args) {
        Jedis jedis = new Jedis(RedisConnectUtil.redisIp, RedisConnectUtil.redisPort);
        String userId = "abc";
        String key = keyFor(userId);
        System.out.println("key" + key);
        jedis.setnx(key, String.valueOf(5));
        System.out.println(doubleAccount(jedis, userId));
        jedis.close();
    }

    private static int doubleAccount(Jedis jedis, String userId) {
        String key = keyFor(userId);
        while (true) {
            jedis.watch(key); // 确保事务执行过程中,key没有发生变化
            int value = Integer.parseInt(jedis.get(key));
            value *= 2;
            Transaction tx = jedis.multi();
            tx.set(key, String.valueOf(value));
            List<Object> res = tx.exec();
            if (null != res) {
                break; // 执行成功
            } // 等于null,继续重试
        }
        return Integer.parseInt(jedis.get(key)); // 返回余额
    }

    private static String keyFor(String userId) {
        return String.format("account_{}", userId);
    }
}

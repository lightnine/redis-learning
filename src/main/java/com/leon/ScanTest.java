package com.leon;

import com.leon.util.RedisConnectUtil;
import redis.clients.jedis.Jedis;

/**
 * 测试scan指令
 * @author leon
 * @since 2020/3/14 18:54
 */
public class ScanTest {
    private Jedis jedis;

    public ScanTest(Jedis jedis) {
        this.jedis = jedis;
    }
    public void generateData() {
        for (int i = 0; i < 10000; i++) {
            String key = "key" + i;
            jedis.set(key, String.valueOf(i));
        }
    }
    public static void main(String[] args) {
        Jedis jedis = new Jedis(RedisConnectUtil.redisIp, RedisConnectUtil.redisPort);
//        System.out.println(jedis.ping());
        System.out.println(jedis.get("key1"));
//        ScanTest scanTest = new ScanTest(jedis);
//        scanTest.generateData();

    }
}

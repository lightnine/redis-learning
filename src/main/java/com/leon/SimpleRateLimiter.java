package com.leon;

import com.leon.util.RedisConnectUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * 用redis实现简单的限流,这里实现用户在指定的时间内容发帖的上限值
 * @author leon
 * @since 2020/3/11 22:07
 */
public class SimpleRateLimiter {
    private Jedis jedis;
    public SimpleRateLimiter(Jedis jedis) {
        this.jedis = jedis;
    }

    public boolean isActionAllowed(String userId, String actionKey, int period, int maxCount) {
        String key = String.format("hist:%s:%s", userId, actionKey);
        System.out.println("key:" + key);
        long nowTs = System.currentTimeMillis();
        Pipeline pipe = jedis.pipelined();
        pipe.multi();
        pipe.zadd(key, nowTs, "" + nowTs);
        // 移除过去一分钟之前的数据
        pipe.zremrangeByScore(key, 0, nowTs - period * 1000);
        Response<Long> count = pipe.zcard(key);
        pipe.expire(key, period + 1);
        pipe.exec();
        pipe.close();
        return count.get() <= maxCount;
    }

    public static void main(String[] args) {
        String redisIp = RedisConnectUtil.redisIp;
        Integer redisPort = RedisConnectUtil.redisPort;
        Jedis jedis = new Jedis(redisIp, redisPort);
        SimpleRateLimiter limiter = new SimpleRateLimiter(jedis);
        for (int i = 0; i < 20; i++) {
            System.out.println(limiter.isActionAllowed("leon", "replay", 60, 5));
        }
    }
}

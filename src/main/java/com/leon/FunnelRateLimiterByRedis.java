package com.leon;

import com.leon.util.RedisConnectUtil;
import redis.clients.jedis.Jedis;

import org.apache.commons.collections4.CollectionUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * 采用redis实现漏斗
 * @author leon
 * @since 2020/3/14 9:22
 */
public class FunnelRateLimiterByRedis {
    private Jedis jedis;
    public FunnelRateLimiterByRedis(Jedis jedis) {
        this.jedis = jedis;
    }
    /**
     *
     * @param funnelName
     * @param capacity
     * @param leakingRate
     * @return 成功返回OK,失败返回NO
     */
    public String createFunnel(String funnelName, int capacity, float leakingRate) {
        Map<String, String> values = jedis.hgetAll(funnelName);
        if (null != values && !CollectionUtils.isEmpty(values.keySet())) {
            return "NO";
        }
        long leakingTs = System.currentTimeMillis();
        Map<String, String> fields = new HashMap<>(8);
        fields.put("capacity", String.valueOf(capacity));
        fields.put("leakingRate", String.valueOf(leakingRate));
        fields.put("leftQuota", String.valueOf(capacity));
        fields.put("leakingTs", String.valueOf(leakingTs));
        return jedis.hmset(funnelName, fields);
    }
    public boolean isActionAllowed(String userId, String actionKey) {
        String key = String.format("%s:%s", userId, actionKey);
        return watering(1, key);
    }
    private boolean watering(int quota, String key) {
        makeSpace(key);
        int leftQuota = Integer.parseInt(jedis.hget(key, "leftQuota"));
        if (leftQuota >= quota) {
            leftQuota -= quota;
            jedis.hset(key, "leftQuota", String.valueOf(leftQuota));
            return true;
        }
        return false;
    }
    private void makeSpace(String key) {
        long nowTs = System.currentTimeMillis();
        Map<String, String> values = jedis.hgetAll(key);
        int capacity = Integer.parseInt(values.get("capacity"));
        int leftQuota = Integer.parseInt(values.get("leftQuota"));
        long leakingTs = Long.parseLong(values.get("leakingTs"));
        float leakingRate = Float.parseFloat(values.get("leakingRate"));
        long deltaTs = nowTs - leakingTs;
        int deltaQuota = (int) (deltaTs * leakingRate);
        if (deltaQuota < 0) { // 若间隔时间过长,可能会整数溢出
            values.put("leftQuota", String.valueOf(capacity));
            values.put("leakingTs", String.valueOf(nowTs));
        } else if (deltaQuota > 1 ) {
            leftQuota += deltaQuota;
            leakingTs = nowTs;
            if (leftQuota > capacity) {
                leftQuota = capacity;
            }
            values.put("leftQuota", String.valueOf(leftQuota));
            values.put("leakingTs", String.valueOf(leakingTs));
        }
        jedis.hmset(key, values);
    }
    public static void main(String[] args) {
        Jedis jedis = new Jedis(RedisConnectUtil.redisIp, RedisConnectUtil.redisPort);
        FunnelRateLimiterByRedis funnel = new FunnelRateLimiterByRedis(jedis);
        String key = String.format("%s:%s", "leon", "reply");
        if ("OK".equals(funnel.createFunnel(key, 5, 1))) {
            for (int i = 0; i < 20; i++) {
                System.out.println(funnel.isActionAllowed("leon", "reply"));
            }
        }
        jedis.del(key);
    }
}

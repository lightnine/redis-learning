package com.leon.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * 连接redis的工具类
 * @author leon
 * @since 2020/3/8 14:25
 */
public class RedisConnectUtil {
    public static String redisIp;
    public static Integer redisPort;
    static {
        Yaml yaml = new Yaml();
        InputStream inputStream = RedisConnectUtil.class.getClassLoader()
.getResourceAsStream("redis.yml");
        Map<String, Object> map = yaml.load(inputStream);
//        System.out.println(map);
        Map redis = (Map) map.get("redis");
        redisIp = (String) redis.get("ip");
        redisPort = (Integer) redis.get("port");
    }

    /**
     * 获取redis connect
     * @return redis connection
     */
    public static StatefulRedisConnection<String, String> getConn() {
        RedisClient redisClient = RedisClient.create(RedisURI.create(redisIp, redisPort));
        return redisClient.connect();
    }

    public static void main(String[] args) {
        System.out.println(redisIp);
        System.out.println(redisPort);
        System.out.println(getConn());
    }
}

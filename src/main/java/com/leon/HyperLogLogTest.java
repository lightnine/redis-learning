package com.leon;

import com.leon.util.RedisConnectUtil;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Arrays;
import java.util.List;

/**
 * 测试往HyperLogLog中添加数据,有多大的错误率
 * @author leon
 * @since 2020/3/10 19:17
 */
public class HyperLogLogTest {
    private RedisCommands<String, String> commands;
    private static final Integer TEST_NUM = 1000;
    private static final Integer TEST_NUM2 = 10000;
    public HyperLogLogTest(RedisCommands<String, String> commands) {
        this.commands = commands;
    }

    /**
     * 测试当放到HyperLogLog中,其中包含的元素个数与放置进去的不一致
     */
    public void testWhenErrorOccur(String key) {
        for (int i = 0; i < TEST_NUM; i++) {
            commands.pfadd(key, "user" + i);
            long total = commands.pfcount(key);
            if (total != i + 1) {
                System.out.printf("testWhenErrorOccur, redis total: %d, put times: %d \n", total, i + 1);
                break;
            }
        }
        commands.del(key);
    }
    public void testManyError(String key) {
        for (int i = 0; i < TEST_NUM2; i++) {
            commands.pfadd(key, "user" + i);
        }
        long total = commands.pfcount(key);
        System.out.printf("testManyError, redis total: %d, put times: %d \n", total, TEST_NUM2);
        commands.del(key);
    }
    public void testPfCountVsPfMerge(List<String> keys) {
        for (String key : keys) {
            for (int i = 0; i < 1000; i++) {
                commands.pfadd(key, key + i);
            }
        }
        long countNum = 0;
        String[] strings = new String[keys.size()];
        keys.toArray(strings);
        for (String key : keys) {
            countNum += commands.pfcount(key);
        }
        commands.pfmerge("mergeKey", strings);
        long mergeNum = commands.pfcount("mergeKey");
        System.out.printf("testPfCountVsPfMerge, countNum: %d, mergeNum: %d", countNum, mergeNum);
        commands.del("mergeKey");
        commands.del(strings);
    }
    public static void main(String[] args) {
        StatefulRedisConnection<String, String> connection = RedisConnectUtil.getConn();
        RedisCommands<String, String> commands = connection.sync();
        HyperLogLogTest hyperLogLogTest = new HyperLogLogTest(commands);
        hyperLogLogTest.testWhenErrorOccur("leon");
        hyperLogLogTest.testManyError("melody");
        List<String> keys = Arrays.asList("key1", "key2", "key3");
        hyperLogLogTest.testPfCountVsPfMerge(keys);
        connection.close();
    }
}

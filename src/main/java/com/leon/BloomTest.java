package com.leon;

import com.leon.util.RedisConnectUtil;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;

/**
 * 展示布隆过滤器
 *
 * @author leon
 * @since 2020/3/10 22:29
 */
public class BloomTest {
    private RedisCommands<String, String> commands;

    public BloomTest(RedisCommands<String, String> commands) {
        this.commands = commands;
    }

    public void test1(String key, int total) {
        commands.del(key);
        RedisCodec<String, String> codec = StringCodec.UTF8;

        String response = commands.dispatch(CommandType.XADD, new StatusOutput<>(codec),
                new CommandArgs<>(codec)
                        .addKey(key)
                        .addValue("user1"));
        System.out.println(response);
//        for (int i = 0; i < total; i++) {
//        }
    }
    public static void main(String[] args) {
        StatefulRedisConnection<String, String> connection = RedisConnectUtil.getConn();
        RedisCommands<String, String> commands = connection.sync();
        BloomTest bloomTest = new BloomTest(commands);
        bloomTest.test1("xx", 10);
    }
}

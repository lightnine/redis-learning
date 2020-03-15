package com.leon;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.leon.util.RedisConnectUtil;
import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * redis实现延迟队列
 * @author leon
 * @since 2020/3/1 10:17
 */
public class RedisDelayingQueue<T> {
    /**
     * 延迟队列中的任务
     * @param <T>
     */
    static class TaskItem<T> {
        public String id;
        public T msg;
    }
    private Type TaskType = new TypeReference<TaskItem<T>>(){}.getType();
    private RedisCommands<String, String> commands;
    private String queueKey;

    public RedisDelayingQueue(RedisCommands<String, String> commands, String queueKey) {
        this.commands = commands;
        this.queueKey = queueKey;
    }

    public void delay(T msg) {
        TaskItem task = new TaskItem();
        task.id = UUID.randomUUID().toString();
        task.msg = msg;
        String s = JSON.toJSONString(task);
        // 给指定队列添加指定score的元素
        commands.zadd(queueKey, System.currentTimeMillis() + 5000, s);
    }
    public void loop() {
        while (!Thread.interrupted()) {
            // 获取score在[0, System.currentTimeMillis()]之间的元素, Limit限定只取第一个
            List values = commands.zrangebyscore(queueKey, Range.create(0, System.currentTimeMillis()), Limit.create(0, 1));
            if (values.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    break;
                }
                continue;
            }
            String s = (String) values.iterator().next();
            // zrem将s从queueKey中移除
            if (commands.zrem(queueKey, s) > 0) {
                TaskItem task = JSON.parseObject(s, TaskType);
                String taskJson = JSON.toJSONString(task);
                this.handleMsg(taskJson);
            }
        }
    }

    public void handleMsg(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {
        // 1. 获取redis connect以及command
        StatefulRedisConnection<String, String> connection = RedisConnectUtil.getConn();
        RedisCommands<String, String> commands = connection.sync();
        // 2. 构造延迟队列
        final RedisDelayingQueue<String> queue = new RedisDelayingQueue<>(commands, "q-demo");
        // 3. 生成者线程池
//        ThreadPoolExecutor producerPool = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(2), new ThreadPoolExecutor.DiscardPolicy());

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                queue.delay("codehole" + i);
            }
        });
        Thread consumer = new Thread(queue::loop);
        producer.start();
        consumer.start();
        try {
            // 直到producer执行完,才执行主线程
            producer.join();
            System.out.println("producer");
            Thread.sleep(6000);
            consumer.interrupt();
            consumer.join();
            System.out.println("consumer");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        connection.close();
    }
}

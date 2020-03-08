package com.leon;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

/**
 * redis实现延迟队列
 * @author leon
 * @since 2020/3/1 10:17
 */
public class RedisDelayingQueue<T> {
    static class TaskItem<T> {
        public String id;
        public T msg;
    }
    private Type TaskType = new TypeReference<TaskItem<T>>(){}.getType();
    private Jedis jedis;
    private String queueKey;

    public RedisDelayingQueue(Jedis jedis, String queueKey) {
        this.jedis = jedis;
        this.queueKey = queueKey;
    }

    public void delay(T msg) {
        TaskItem task = new TaskItem();
        task.id = UUID.randomUUID().toString();
        task.msg = msg;
        String s = JSON.toJSONString(task);
        jedis.zadd(queueKey, System.currentTimeMillis() + 5000, s);
    }
    public void loop() {
        while (!Thread.interrupted()) {
            Set values = jedis.zrangeByScore(queueKey, 0, System.currentTimeMillis(), 0, 1);
            if (values.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    break;
                }
                continue;
            }
            String s = (String) values.iterator().next();
            if (jedis.zrem(queueKey, s) > 0) {
                TaskItem task = JSON.parseObject(s, TaskType);
                this.handleMsg((T) task.msg);
            }
        }
    }

    public void handleMsg(T msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis();
        final RedisDelayingQueue<String> queue = new RedisDelayingQueue<>(jedis, "q-demo");
        Thread producer = new Thread() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    queue.delay("codehole" + i);
                }
            }
        };
        Thread consumer = new Thread() {
            public void run() {
                queue.loop();
            }
        };
        producer.start();
        consumer.start();
        try {
            producer.join();
            Thread.sleep(6000);
            consumer.interrupt();
            consumer.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}

package com.leon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 实现漏斗限流
 * @author leon
 * @since 2020/3/13 23:04
 */
public class FunnelRateLimiter {
    // 漏斗实现, 漏斗是以一定的速度流出内容的
    static class Funnel {
        // 漏斗总容量
        int capacity;
        // 出漏斗速度
        float leakingRate;
        // 漏斗剩余的配额
        int leftQuota;
        // 上一次漏水时间
        long leakingTs;

        public Funnel(int capacity, float leakingRate) {
            this.capacity = capacity;
            this.leakingRate = leakingRate;
            this.leftQuota = capacity;
            this.leakingTs = System.currentTimeMillis();
        }
        void makeSpace() {
            long nowTs = System.currentTimeMillis();
            long deltaTs = nowTs - leakingTs;
            int deltaQuota = (int) (deltaTs * leakingRate);
            System.out.println(deltaQuota);
            if (deltaQuota < 0) { // 若间隔时间过长,可能会整数溢出
                this.leftQuota = capacity;
                this.leakingTs = nowTs;
                return;
            }
            if (deltaQuota < 1) { // 漏斗中腾出的空间太小,最小单位是1
                return;
            }
            this.leftQuota += deltaQuota; // 增加剩余空间
            this.leakingTs = nowTs;
            if (this.leftQuota > this.capacity) { // 剩余空间不能大于容量
                this.leftQuota = this.capacity;
            }
        }

        boolean watering(int quota) {
            makeSpace();
            if (this.leftQuota >= quota) {
                this.leftQuota -= quota;
                return true;
            }
            return false;
        }
    }

    private Map<String, Funnel> funnels = new HashMap<>();

    public boolean isActionAllowed(String userId, String actionKey, int capacity, float leakingRate) {
        String key = String.format("%s:%s", userId, actionKey);
        Funnel funnel = funnels.get(key);
        if (funnel == null) {
            funnel = new Funnel(capacity, leakingRate);
            funnels.put(key, funnel);
        }
        return funnel.watering(1);
    }

    public static void main(String[] args) throws InterruptedException {
        FunnelRateLimiter funnelRateLimiter = new FunnelRateLimiter();
        for (int i = 0; i < 20; i++) {
            // 加入sleep,可以让漏斗有时间流水
//            TimeUnit.SECONDS.sleep(1);
            System.out.println(funnelRateLimiter.isActionAllowed("leon", "reply", 5, 1));
        }
    }
}

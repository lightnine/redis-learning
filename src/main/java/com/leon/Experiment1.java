package com.leon;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 展示随机数最后连续为0的个数与随机总数的关系
 *
 * @author leon
 * @since 2020/3/10 20:06
 */
public class Experiment1 {
    static class BitKeeper {
        private int maxbits;

        public void random() {
            long value = ThreadLocalRandom.current().nextLong(2L << 32);
            int bits = lowZeros(value);
            if (bits > this.maxbits) {
                this.maxbits = bits;
            }
        }

        private int lowZeros(long value) {
            int i = 1;
            for (; i < 32; i++) {
                // 先将value右移i位,然后在左移i位; 然后比较是否与之前的值相等;
                // 如果不相等,则表明已经碰到1了
                if (value >> i << i != value) {
                    break;
                }
            }
            return i - 1;
        }
    }

    private int n;
    private BitKeeper keeper;

    public Experiment1(int n, BitKeeper keeper) {
        this.n = n;
        this.keeper = keeper;
    }

    public void work() {
        // 产生n个随机数,然后获取连续0的个数是多少个
        for (int i = 0; i < n; i++) {
            this.keeper.random();
        }
    }

    public void debug() {
        System.out.printf("总数: %d, %.2f, 连续0的个数:%d\n", this.n, Math.log(this.n) / Math.log(2), this.keeper.maxbits);
    }

    public static void main(String[] args) {
        for (int i = 1000; i < 100000; i += 100) {
            Experiment1 exp = new Experiment1(i, new BitKeeper());
            exp.work();
            exp.debug();
        }
    }
}

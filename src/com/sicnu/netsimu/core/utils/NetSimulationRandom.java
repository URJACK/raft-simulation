package com.sicnu.netsimu.core.utils;

import java.util.Random;

/**
 * 网络仿真 随机类
 * <p>
 * 在本次网络仿真的活动中，用户编写的所有随机数，都应该使用该随机类来产生。
 * 这样可以确保整个的仿真结果，能够使用随机类进行确定。
 */
public class NetSimulationRandom {

    static Random random;
    static Long netRandomSeed = null;

    static {
        random = new Random();
    }

    public static void setNetRandomSeed(long seed) {
        netRandomSeed = seed;
        random = new Random(netRandomSeed);
    }

    public static int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public static float nextFloat() {
        return random.nextFloat();
    }

    public static double nextDouble() {
        return random.nextDouble();
    }

    public static Long getNetRandomSeed() {
        return netRandomSeed;
    }
}

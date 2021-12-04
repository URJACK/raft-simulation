package com.sicnu.netsimu.core.utils;

import java.util.Random;

public class NetsimuRandom {

    static Random random;

    {
        random = new Random();
    }

    public static void setRandomSeed(long seed) {
        random = new Random(seed);
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
}

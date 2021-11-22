package com.sicnu.raftsimu.core.mote.utils;

import com.sicnu.raftsimu.core.mote.Mote;

public class MoteCalculate {
    static float eulaDistance(Mote a, Mote b) {
        float ySpan = Math.abs(a.getY() - b.getY());
        float xSpan = Math.abs(a.getX() - b.getX());
        return (float) Math.sqrt(ySpan * ySpan + xSpan * xSpan);
    }
}

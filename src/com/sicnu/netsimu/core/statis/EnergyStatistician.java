package com.sicnu.netsimu.core.statis;

import com.sicnu.netsimu.core.mote.Mote;

/**
 * 能耗统计者 每个节点都持有一个能耗统计者
 */
public class EnergyStatistician implements Statistician<Float> {
    Mote mote;

    /**
     * 能耗统计者
     *
     * @param mote 节点引用
     */
    public EnergyStatistician(Mote mote) {
        this.mote = mote;
    }

    @Override
    public void addValue(String key, Float value) {
        System.out.println("DEBUG " + mote.getMoteId() + " " + key + " cost " + value);
    }

    @Override
    public Float getValue(String key) {
        return null;
    }
}

package com.sicnu.netsimu.core.statis;

import com.sicnu.netsimu.core.mote.Mote;

import java.util.HashMap;
import java.util.Map;

/**
 * 能耗统计者 每个节点都持有一个能耗统计者
 */
public class EnergyStatistician implements Statistician<Float> {
    Mote mote;
    float sum;
    HashMap<String, Float> map;

    /**
     * 能耗统计者
     *
     * @param mote 节点引用
     */
    public EnergyStatistician(Mote mote) {
        this.mote = mote;
        map = new HashMap<>();
        clear();
    }

    @Override
    public void addValue(String key, Float value) {
        Float defaultVal = map.getOrDefault(key, 0f);
        map.put(key, defaultVal + value);
        sum += value;
//        System.out.println("DEBUG " + mote.getMoteId() + " " + key + " cost " + value);
    }

    @Override
    public Float getValue(String key) {
        return map.get(key);
    }

    @Override
    public Float getAllSummary() {
        return sum;
    }

    @Override
    public void clear() {
        sum = 0f;
        map.clear();
    }
}

package com.sicnu.netsimu.core.statis;

import com.sicnu.netsimu.core.node.Node;

import java.util.HashMap;

/**
 * 能耗统计者 每个节点都持有一个能耗统计者
 */
public class EnergyStatistician implements Statistician<Float> {
    Node node;
    float sum;
    HashMap<String, Float> map;

    /**
     * 能耗统计者
     *
     * @param node 节点引用
     */
    public EnergyStatistician(Node node) {
        this.node = node;
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

    public Float getAllSummary() {
        return sum;
    }

    @Override
    public void clear() {
        sum = 0f;
        map.clear();
    }
}

package com.sicnu.netsimu.core.statis;

import com.sicnu.netsimu.raft.annotation.AllowNull;

import java.util.HashMap;

public class TransmitStatistician implements Statistician<Integer> {
    HashMap<Integer, Integer> map = new HashMap<>();

    /**
     * 在指定类中，增加一条统计数据
     *
     * @param key   指定的Mote的id号
     * @param value 统计数据值
     */
    @Override
    public void addValue(String key, Integer value) {
        Integer moteId = Integer.parseInt(key);
        Integer times = map.getOrDefault(moteId, 0);
        map.put(moteId, times + 1);
    }

    /**
     * 获取到一类统计数据值
     *
     * @param key 指定的Mote的id号
     * @return 该指定类已经统计的数据值
     */
    @Override
    @AllowNull
    public Integer getValue(String key) {
        Integer moteId = Integer.parseInt(key);
        return map.getOrDefault(moteId, 0);
    }

    /**
     * 清空已经记录的所有统计
     */
    @Override
    public void clear() {
        map.clear();
    }
}

package com.sicnu.netsimu.core.statis;

/**
 * 统计者接口
 *
 * @param <T> 需要被统计的类型
 */
public interface Statistician<T> {
    /**
     * 增加一条统计数据
     *
     * @param key
     * @param value
     */
    void addValue(String key, T value);

    T getValue(String key);
}

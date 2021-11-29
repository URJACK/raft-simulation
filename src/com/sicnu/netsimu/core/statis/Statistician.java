package com.sicnu.netsimu.core.statis;

/**
 * 统计者接口
 *
 * @param <T> 需要被统计的类型
 */
public interface Statistician<T> {
    /**
     * 在指定类中，增加一条统计数据
     *
     * @param key   指定类名
     * @param value 统计数据值
     */
    void addValue(String key, T value);

    /**
     * 获取到一类统计数据值
     *
     * @param key 指定类名
     * @return 该指定类已经统计的数据值
     */
    T getValue(String key);

    /**
     * @return 所有类的统计数据值之和
     */
    T getAllSummary();

    /**
     * 清空已经记录的所有统计
     */
    void clear();
}

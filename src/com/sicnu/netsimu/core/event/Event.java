package com.sicnu.netsimu.core.event;

import lombok.Data;

/**
 * 事件类
 * 所有的Event都会被EventManager放入事件队列（以triggerTime排序的优先队列）中进行管理
 */
@Data
public abstract class Event implements Comparable<Event> {
    //触发时间
    protected long triggerTime;

    public Event() {
    }

    /**
     * @param triggerTime 事件的触发时间
     */
    public Event(long triggerTime) {
        this.triggerTime = triggerTime;
    }

    /**
     * 每个事件都有自己的专属方法。
     */
    public abstract void work();

    @Override
    public int compareTo(Event o) {
        return (int) (this.triggerTime - o.triggerTime);
    }
}
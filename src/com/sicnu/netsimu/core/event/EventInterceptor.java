package com.sicnu.netsimu.core.event;

import com.sicnu.netsimu.core.NetSimulator;

/**
 * 事件拦截器，常常在特定事件发生前后触发逻辑
 * 用来配合仿真系统进行相关的数据统计操作
 */
public abstract class EventInterceptor implements Interceptor {
    protected NetSimulator simulator;

    public EventInterceptor(NetSimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public abstract void work(Event event);
}

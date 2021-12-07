package com.sicnu.netsimu.core.event;

import com.sicnu.netsimu.core.NetSimulator;

public abstract class EventInterceptor implements Interceptor {
    protected NetSimulator simulator;

    public EventInterceptor(NetSimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public abstract void work(Event event);
}

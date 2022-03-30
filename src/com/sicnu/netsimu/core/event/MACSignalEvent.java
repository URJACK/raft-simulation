package com.sicnu.netsimu.core.event;

import com.sicnu.netsimu.core.net.mac.channel.Channel;

public class MACSignalEvent extends Event {
    Channel channel;
    boolean isVirtual;

    public MACSignalEvent(long triggerTime, Channel channel, boolean isVirtual) {
        super(triggerTime);
        this.channel = channel;
        this.isVirtual = isVirtual;
    }

    @Override
    public void work() {
        channel.macEventEndingHandler(this);
    }

    public boolean isVirtual() {
        return isVirtual;
    }
}

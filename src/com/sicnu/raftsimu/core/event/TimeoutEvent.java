package com.sicnu.raftsimu.core.event;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.mote.Mote;
import lombok.Data;

@Data
public abstract class TimeoutEvent extends Event {
    long spanTime;
    RaftSimulator simulator;
    Mote selfMote;
    //对isLoop属性的处理，不在事件类本身，而在 EventManager的exec()函数中
    boolean isLoop;

    public TimeoutEvent(long spanTime, boolean isLoop, RaftSimulator simulator, Mote selfMote) {
        // 事件本身要有的延迟 + 仿真器当前的时间 == 触发时间
        super(spanTime + simulator.getNowTime());
        this.spanTime = spanTime;
        this.simulator = simulator;
        this.selfMote = selfMote;
        this.isLoop = isLoop;
    }
}

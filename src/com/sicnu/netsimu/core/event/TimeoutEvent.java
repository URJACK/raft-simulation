package com.sicnu.netsimu.core.event;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.node.Node;
import lombok.Data;

/**
 * 延时动作事件
 * 可以设置为Loop，单个事件结束后，会再次进入EventManager的事件队列中
 */
@Data
public abstract class TimeoutEvent extends Event {
    long spanTime;
    NetSimulator simulator;
    Node selfNode;
    //对isLoop属性的处理，不在事件类本身，而在 EventManager的exec()函数中
    boolean isLoop;

    /**
     * @param spanTime  延时间隔
     * @param isLoop    是否循环
     * @param simulator 模拟器引用
     * @param selfNode  调用setTimeout的节点引用
     */
    public TimeoutEvent(long spanTime, boolean isLoop, NetSimulator simulator, Node selfNode) {
        // 事件本身要有的延迟 + 仿真器当前的时间 == 触发时间
        super(spanTime + simulator.getTime());
        this.spanTime = spanTime;
        this.simulator = simulator;
        this.selfNode = selfNode;
        this.isLoop = isLoop;
    }
}

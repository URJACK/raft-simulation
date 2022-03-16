package com.sicnu.netsimu.core.event;

import com.sicnu.netsimu.core.node.Node;
import lombok.Data;

/**
 * 第一类事件，传输事件
 * 它的功能与core.event.trans.TransmissionManager、core.mote.Mote
 * 都进行了深度的绑定
 */
@Data
public class TransmissionEvent extends Event {
    /**
     * 数据包的发送者
     */
    Node sender;
    /**
     * 数据包的接受者
     */
    Node receiver;
    /**
     * 数据包
     */
    byte[] packet;

    /**
     * @param triggerTime 到达时间
     * @param receiver    接收的节点（引用对象）
     * @param packet      发送的数据包
     */
    public TransmissionEvent(long triggerTime, Node sender, Node receiver, byte[] packet) {
        super(triggerTime);
        this.sender = sender;
        this.receiver = receiver;
        this.packet = packet;
    }

    @Override
    public void work() {
//        receiver.netReceive(packet);
        receiver.call("netReceive", (Object) packet);
    }
}

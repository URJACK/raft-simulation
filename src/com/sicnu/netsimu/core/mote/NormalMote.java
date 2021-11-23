package com.sicnu.netsimu.core.mote;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import lombok.Data;

@Data
public class NormalMote extends Mote {
    /**
     * @param simulator 模拟器对象引用
     * @param moteId    节点的id
     * @param x         节点的x坐标
     * @param y         节点的y坐标
     */
    public NormalMote(NetSimulator simulator, int moteId, float x, float y) {
        super(simulator, moteId, x, y);
    }

    @Override
    public void init() {
        TimeoutEvent event = new TimeoutEvent(500, true, simulator, this) {
            @Override
            public void work() {
                print("我是节点");
            }
        };
        setTimeout(event);
    }

    @Override
    public void netReceive(TransmissionPacket packet) {
        if (!containAddress(packet.getDesIp()) || !containPort(packet.getDesPort())) {
            //如果自身不符合条件
            return;
        }
        print("Received " + packet.getData() + " From " + packet.getSrcIp() + ":" + packet.getSrcPort());
    }

}

package com.sicnu.raftsimu.core.mote;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.event.trans.TransmissionManager;
import com.sicnu.raftsimu.core.event.trans.TransmissionPacket;
import lombok.Data;

@Data
public class NormalMote extends Mote {
    /**
     * @param simulator 模拟器对象引用
     * @param moteId    节点的id
     * @param x         节点的x坐标
     * @param y         节点的y坐标
     */
    public NormalMote(RaftSimulator simulator, int moteId, float x, float y) {
        super(simulator, moteId, x, y);
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

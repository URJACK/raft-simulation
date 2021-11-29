package com.sicnu.netsimu.raft.mote;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.statis.EnergyCost;

/**
 * Raft节点
 */
public class RaftMote extends Mote {
    /**
     * @param simulator 模拟器引用
     * @param moteId    节点Id
     * @param x         节点x坐标
     * @param y         节点y坐标
     */
    public RaftMote(NetSimulator simulator, int moteId, float x, float y) {
        super(simulator, moteId, x, y, RaftMote.class);
    }

    @Override
    @EnergyCost(10f)
    public void init() {

    }

    @Override
    @EnergyCost(30f)
    public void netReceive(TransmissionPacket packet) {

    }
}

package com.sicnu.raftsimu.core.event.trans;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.mote.Mote;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * TransmissionManager 传输管理者
 * 它需要耦合两个对象：RaftSimulator 和 Mote
 * 每次RaftSimulator 进行 “节点创建”、“节点删除”，
 * 都会使得TransmissionManager，对各个节点之间的关系进行重算。
 */
public class TransmissionManager {

    RaftSimulator simulator;

    public TransmissionManager(RaftSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * 取得这个节点的传输范围内的其他节点
     *
     * @param mote
     * @return
     */
    public List<Neighbor> getNeighbors(Mote mote) {
        return null;
    }

    /**
     * 根据两点距离计算他们的传输时间
     * @param distance 两点距离
     * @return 传输时间
     */
    public long calcTransmissionTime(float distance) {
        return (long) distance;
    }

    /**
     * 邻居对象
     * 这是为了防止重复计算距离而设立的一个类
     */
    @Data
    @AllArgsConstructor
    public static class Neighbor {
        Mote mote;
        float distance;
    }
}

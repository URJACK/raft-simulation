package com.sicnu.netsimu.core.event.trans;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.mote.utils.MoteCalculate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * TransmissionManager 传输管理者
 * 它需要耦合两个对象：RaftSimulator 和 Mote
 * 每次RaftSimulator 进行 “节点创建”、“节点删除”，
 * 都会使得TransmissionManager，对各个节点之间的关系进行重算。
 */
public class TransmissionManager {
    //最大传输距离
    private static final float MAX_TRANSMIT_DISTANCE = 200;
    NetSimulator simulator;
    HashMap<Integer, LinkedList<Neighbor>> table;

    /**
     * @param simulator 仿真器引用
     */
    public TransmissionManager(NetSimulator simulator) {
        this.simulator = simulator;
        table = new HashMap<>();
    }

    /**
     * 取得这个节点的传输范围内的其他节点
     *
     * @param mote
     * @return
     */
    public List<Neighbor> getNeighbors(Mote mote) {
        return table.get(mote.getMoteId());
    }

    /**
     * 根据两点距离计算他们的传输时间
     *
     * @param distance 两点距离
     * @return 传输时间
     */
    public long calcTransmissionTime(float distance) {
        return (long) distance;
    }

    /**
     * 增加节点，刷新状态信息
     *
     * @param nodeId 新增的节点id号
     */
    public void addNode(int nodeId) {
        MoteManager moteManager = simulator.getMoteManager();
        ArrayList<Mote> motes = moteManager.getAllMotes();
        LinkedList<Neighbor> neighborsOfA = table.computeIfAbsent(nodeId, k -> new LinkedList<>());
        Mote moteA = moteManager.getMote(nodeId);
        for (int i = 0; i < motes.size(); i++) {
            Mote moteB = motes.get(i);
            LinkedList<Neighbor> neighborsOfB = table.computeIfAbsent(moteB.getMoteId(), k -> new LinkedList<>());
            if (moteB.getMoteId() == (nodeId)) {
                //不计算自己和自己
                continue;
            }
            float distance = MoteCalculate.eulaDistance(moteB, moteA);
            if (distance > MAX_TRANSMIT_DISTANCE) {
                //距离太远无法成为邻居
                continue;
            }
            Neighbor neighborOfA = new Neighbor(moteB, distance);
            Neighbor neighborOfB = new Neighbor(moteA, distance);
            neighborsOfA.add(neighborOfA);
            neighborsOfB.add(neighborOfB);
        }
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

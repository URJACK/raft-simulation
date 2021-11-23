package com.sicnu.netsimu.core.mote;

import com.sicnu.netsimu.core.NetSimulator;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 节点管理器，下属于Simulator
 * 存储了节点的相关信息
 */
public class MoteManager {
    ArrayList<Mote> motes;
    HashMap<Integer, Mote> moteRecorder;
    NetSimulator simulator;

    /**
     * @param simulator 仿真器对象引用
     */
    public MoteManager(NetSimulator simulator) {
        motes = new ArrayList<>();
        moteRecorder = new HashMap<>();
        this.simulator = simulator;
    }

    /**
     * 节点是否已经存在
     *
     * @param nodeId 节点id
     * @return
     */
    public boolean containMote(int nodeId) {
        return moteRecorder.containsKey(nodeId);
    }

    /**
     * 添加节点，节点的id不可以之前存在
     *
     * @param nodeId 节点的id
     * @param x      节点的x坐标
     * @param y      节点的y坐标
     * @return 成功添加的新节点（失败则为null）
     */
    public Mote addMote(int nodeId, float x, float y) {
        if (moteRecorder.containsKey(nodeId)) {
            //如果已经存在了这个节点
            return null;
        }
        NormalMote mote = new NormalMote(simulator, nodeId, x, y);
        motes.add(mote);
        moteRecorder.put(nodeId, mote);
        return mote;
    }

    /**
     * 获取节点
     *
     * @param nodeId 节点的id
     * @return 通过节点id获得的节点
     */
    public Mote getMote(int nodeId) {
        return moteRecorder.get(nodeId);
    }

    /**
     * 将所有节点都进行获取
     * @return 所有节点
     */
    public ArrayList<Mote> getAllMotes() {
        return motes;
    }
}

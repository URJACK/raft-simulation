package com.sicnu.raftsimu.core.mote;

import com.sicnu.raftsimu.core.RaftSimulator;

import java.util.ArrayList;
import java.util.HashMap;

public class MoteManager {
    ArrayList<Mote> motes;
    HashMap<Integer, Mote> moteRecorder;
    RaftSimulator simulator;

    public MoteManager(RaftSimulator simulator) {
        motes = new ArrayList<>();
        moteRecorder = new HashMap<>();
        this.simulator = simulator;
    }

    public boolean containMote(int nodeId) {
        return moteRecorder.containsKey(nodeId);
    }

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

    public Mote getMote(int nodeId) {
        return moteRecorder.get(nodeId);
    }

    public ArrayList<Mote> getMotes() {
        return motes;
    }
}

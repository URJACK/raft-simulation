package com.sicnu.raftsimu.core.mote;

import com.sicnu.raftsimu.core.RaftSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

    public void addMote(int nodeId, float x, float y) {
        BasicMote mote = new BasicMote(simulator, nodeId, x, y);
        motes.add(mote);
        moteRecorder.put(nodeId, mote);
    }

    public Mote getMote(int nodeId) {
        return moteRecorder.get(nodeId);
    }
}

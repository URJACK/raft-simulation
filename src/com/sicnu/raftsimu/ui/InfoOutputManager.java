package com.sicnu.raftsimu.ui;

import com.sicnu.raftsimu.core.RaftSimulator;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class InfoOutputManager {
    Deque<InfoItem> deque;
    RaftSimulator simulator;

    public InfoOutputManager(RaftSimulator simulator) {
        deque = new LinkedList<>();
        this.simulator = simulator;
    }

    public void pushInfo(long time, int moteId, String info) {
        deque.addLast(new InfoItem(time, moteId, info));
    }

    public void outputInfo() {
        while (!deque.isEmpty()) {
            InfoItem infoItem = deque.pollFirst();
            System.out.println(infoItem);
        }
    }

    @AllArgsConstructor
    public static class InfoItem {
        long time;
        int moteId;
        String info;

        @Override
        public String toString() {
            return time + ", " +
                    "Mote " + moteId + ":" +
                    info;
        }
    }
}

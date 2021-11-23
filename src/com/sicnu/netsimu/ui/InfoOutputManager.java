package com.sicnu.netsimu.ui;

import com.sicnu.netsimu.core.NetSimulator;
import lombok.AllArgsConstructor;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 信息输出管理器
 */
public class InfoOutputManager {
    //还没有被输出的信息对象
    Deque<InfoItem> deque;
    //仿真器的引用
    NetSimulator simulator;

    public InfoOutputManager(NetSimulator simulator) {
        deque = new LinkedList<>();
        this.simulator = simulator;
    }

    /**
     * 添加一条需要被输出的信息
     *
     * @param time   信息所属的时间戳
     * @param moteId 输出信息的节点编号
     * @param info   输出的信息
     */
    public void pushInfo(long time, int moteId, String info) {
        deque.addLast(new InfoItem(time, moteId, info));
    }

    /**
     * 触发输出动作，将还未输出的信息都进行输出
     * 通常情况下，该函数被 DisplayCommand.java 进行调用
     */
    public void outputInfo() {
        while (!deque.isEmpty()) {
            InfoItem infoItem = deque.pollFirst();
            System.out.println(infoItem);
        }
    }

    /**
     * 我们存入打印队列中的对象
     * 包含了打印时间，输出者，打印内容三项信息
     */
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

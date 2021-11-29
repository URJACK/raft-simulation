package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.statis.Statistician;

import java.util.*;

public class IncrementalSummarizerWithConsole extends IncrementalSummarizer {
    HashMap<Integer, List<Float>> incrementalMap;
    public static final String OUTPUT = "OUTPUT";
    public static final String CALC = "CALC";

    /**
     * @param simulator 网络模拟器对象引用
     */
    public IncrementalSummarizerWithConsole(NetSimulator simulator) {
        super(simulator);
        incrementalMap = new HashMap<>();
    }

    @Override
    protected void processOutput() {
        for (Map.Entry<Integer, List<Float>> entry : incrementalMap.entrySet()) {
            Integer moteId = entry.getKey();
            List<Float> list = entry.getValue();
            System.out.print("Mote " + moteId + " : ");
            System.out.println(list);
        }
    }


    /**
     * 基础增量计算
     * 所有的IncrementalSummarizer都应当调用该方法计算各个数据的增量
     */
    protected void processBasicCalc() {
        MoteManager moteManager = simulator.getMoteManager();
        ArrayList<Mote> allMotes = moteManager.getAllMotes();
        for (Mote mote : allMotes) {
            Statistician<Float> energyStatistician = mote.getEnergyStatistician();
            //获得每个节点的能耗
            Float statisticianAllSummary = energyStatistician.getAllSummary();
            //清空这个时间点的能耗记录
            energyStatistician.clear();
            //将这次到上次调用之间时段 “时段能耗数据” 进行统计
            List<Float> list = incrementalMap.computeIfAbsent(mote.getMoteId(), k -> new LinkedList<>());
            //将“时段能耗数据”塞入对应节点的列表中
            list.add(statisticianAllSummary);
        }
    }
}

package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.statis.EnergyStatistician;

import java.util.ArrayList;

/**
 * 总量总结器
 */
public class BasicTotalSummarizer extends Summarizer {
    /**
     * @param simulator 网络模拟器对象引用
     */
    public BasicTotalSummarizer(NetSimulator simulator) {
        super(simulator);
    }

    /**
     * TotalSummarizer目前参数无作用
     * @param param 触发动作参数
     */
    @Override
    public void summarize(String param) {
        MoteManager moteManager = simulator.getMoteManager();
        ArrayList<Mote> allMotes = moteManager.getAllMotes();
        for (Mote mote : allMotes) {
            EnergyStatistician statistician = mote.getSingleMoteEnergyStatistician();
            System.out.println(mote.getMoteId() + " : " + statistician.getAllSummary());
        }
    }

}
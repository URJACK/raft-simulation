package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.net.TransmissionManager;
import com.sicnu.netsimu.core.statis.EnergyStatistician;
import com.sicnu.netsimu.core.statis.Statistician;
import com.sicnu.netsimu.core.statis.TransmitStatistician;

import java.util.*;

/**
 * 控制台输出 增量总结器
 */
public class IncrementalSummarizerWithConsole extends IncrementalSummarizer {
    /**
     * 能耗记录器
     * <pre>
     * {moteId, 该节点对应的能耗记录}
     * </pre>
     */
    HashMap<Integer, List<Float>> energyCalcMap;

    /**
     * @param simulator 网络模拟器对象引用
     */
    public IncrementalSummarizerWithConsole(NetSimulator simulator) {
        super(simulator);
        energyCalcMap = new HashMap<>();
    }

    /**
     * 当传入的param是 OUTPUT的时候，summarize() 会调用该方法。
     */
    @Override
    protected void processOutput() {
        TransmissionManager transmissionManager = simulator.getTransmissionManager();
        TransmitStatistician successSendStatistician = transmissionManager.getSuccessSendStatistician();
        TransmitStatistician failedSendStatistician = transmissionManager.getFailedSendStatistician();
        TransmitStatistician successReceiveStatistician = transmissionManager.getSuccessReceiveStatistician();
        TransmitStatistician failedReceiveStatistician = transmissionManager.getFailedReceiveStatistician();

        for (Map.Entry<Integer, List<Float>> entry : energyCalcMap.entrySet()) {
            Integer moteId = entry.getKey();
            List<Float> list = entry.getValue();
            System.out.print("Mote " + moteId + " : ");
            System.out.print(list);
            int sendSuccessTimes = successSendStatistician.getValue(String.valueOf(moteId));
            int sendFailedTimes = failedSendStatistician.getValue(String.valueOf(moteId));
            int receiveSuccessTimes = successReceiveStatistician.getValue(String.valueOf(moteId));
            int receiveFailedTimes = failedReceiveStatistician.getValue(String.valueOf(moteId));

            float sendSuccessRate = (float) sendSuccessTimes / (sendSuccessTimes + sendFailedTimes);
            float sendFailedRate = (float) sendFailedTimes / (sendSuccessTimes + sendFailedTimes);
            float receiveSuccessRate = (float) receiveSuccessTimes / (receiveSuccessTimes + receiveFailedTimes);
            float receiveFailedRate = (float) receiveFailedTimes / (receiveSuccessTimes + receiveFailedTimes);

            System.out.println();
            System.out.print(" 平均送达率" + sendSuccessRate);
            System.out.print(" 平均发送丢包率" + sendFailedRate);
            System.out.println();
            System.out.print(" 平均接受率" + receiveSuccessRate);
            System.out.print(" 平均接收丢包率" + receiveFailedRate);
            System.out.println();
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
            EnergyStatistician energyStatistician = mote.getEnergyStatistician();
            //获得每个节点的能耗
            Float statisticianAllSummary = energyStatistician.getAllSummary();
            //清空这个时间点的能耗记录
            energyStatistician.clear();
            //将这次到上次调用之间时段 “时段能耗数据” 进行统计
            List<Float> list = energyCalcMap.computeIfAbsent(mote.getMoteId(), k -> new LinkedList<>());
            //将“时段能耗数据”塞入对应节点的列表中
            list.add(statisticianAllSummary);
        }
    }
}

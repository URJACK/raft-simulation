package com.sicnu.raft.ui;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.Command;
import com.sicnu.netsimu.core.event.CommandEvent;
import com.sicnu.netsimu.core.event.Event;
import com.sicnu.netsimu.core.event.TransmissionEvent;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.statis.EnergyStatistician;
import com.sicnu.netsimu.ui.summary.IncrementalSummarizer;

import java.util.*;

import com.sicnu.netsimu.core.net.TransmissionManager;
import com.sicnu.raft.command.RaftOpCommand;
import com.sicnu.raft.mote.RaftMote;
import com.sicnu.raft.role.log.RaftLogTable;

/**
 * 本总结器，可以总结以下几个类别的属性：
 * <pre>
 * 1· 节点能耗 energyCosts
 * 2· 丢包率等传输指标 transmissionTargets
 * 3· 数据同步耗时 dataSyncCosts
 * 4· 发送心跳包的数量 heartbeatsNumbers
 * </pre>
 * Mote中有一个 EnergyStatistician ，它记录了能耗总量
 * <p>
 * TransmissionManager 已经提供了 getStatisticInformationWithId(moteId) 这个方法
 * <p>
 * 关于数据同步的耗时
 *
 * @see Mote
 * @see EnergyStatistician
 * @see TransmissionManager
 */
public class RaftSummarizer extends IncrementalSummarizer {
    /**
     * 同步参数 SYNC_POST
     * Summarizer 可以通过 PreEventInterceptor 传入SYNC_PRE
     *
     * @see RaftCalculateInPreEventInterceptor
     */
    public static final String SYNC_PRE = "SYNC_PRE";
    /**
     * 同步参数 SYNC_POST
     * Summarizer 可以通过 PostEventInterceptor 传入SYNC_POST
     *
     * @see RaftCalculateInPostEventInterceptor
     */
    public static final String SYNC_POST = "SYNC_POST";
    /**
     * 统计节点能耗
     * {moteId, 对应的节点能耗数据(按时间段) }
     */
    HashMap<Integer, List<Float>> energyCalcMap;

    // 平均丢包率等指标，不需要在此处得出，使用

    /**
     * 主要会涉及到 raftValidateEquipInPre() 和 raftValidateCheckInPost() 两个方法
     */
    RaftDataSyncVariable raftDataSyncVariable;


    /**
     * @param simulator 网络模拟器对象引用
     * @param n         网络模拟器节点个数
     */
    public RaftSummarizer(NetSimulator simulator, int n) {
        super(simulator);
        energyCalcMap = new HashMap<>();
        raftDataSyncVariable = new RaftDataSyncVariable(n);
    }

    /**
     * 需要注意的是 param == "SYNC" 时，
     *
     * @param param 触发动作参数，用来控制是否调用 processOutput() 和 raftStaticCalculate()
     */
    @Override
    public void summarize(String param) {
        super.summarize(param);
        if (param.equals(SYNC_PRE)) {
            raftValidateEquipInPre();
        } else if (param.equals(SYNC_POST)) {
            raftValidateCheckInPost();
        }
    }

    /**
     * 当传入的param是 OUTPUT的时候，summarize() 会调用该方法。
     */
    @Override
    protected void processOutput() {
        TransmissionManager transmissionManager = simulator.getTransmissionManager();
        for (Map.Entry<Integer, List<Float>> entry : energyCalcMap.entrySet()) {
            Integer moteId = entry.getKey();

            TransmissionManager.StatisticInfo info = transmissionManager.getStatisticInformationWithId(moteId);

            float sendSuccessRate = info.getSendSuccessRate();
            float sendFailedRate = info.getSendFailedRate();
            float receiveSuccessRate = info.getReceiveSuccessRate();
            float receiveFailedRate = info.getReceiveFailedRate();

            System.out.println();
            System.out.print(" 平均送达率" + sendSuccessRate);
            System.out.print(" 平均发送丢包率" + sendFailedRate);
            System.out.println();
            System.out.print(" 平均接受率" + receiveSuccessRate);
            System.out.print(" 平均接收丢包率" + receiveFailedRate);
            System.out.println();
        }
        System.out.println(raftDataSyncVariable.consensusTimes);
    }

    /**
     * 基础增量计算
     * <p>
     * 无论传入的是 CALC 还是 OUTPUT，
     * summarize都会调用该方法，计算各个数据的增量
     */
    @Override
    protected void processBasicCalc() {
        energyCalc();
    }


    /**
     * 在事件队列处理事件A之前，
     * 如果事件A是 TransmissionEvent 和 CommandEvent，
     * 这两类事件可能会发生日志的修改。
     * <p>
     * 我们通过该方法，获取到还没进行处理的事件A，
     * 从而读取A中的mote对象，之后在 raftValidateCheckInPost() 之中对其内容进行检查
     *
     * @see NetSimulator
     * @see RaftCalculateInPreEventInterceptor
     * @see TransmissionEvent
     * @see CommandEvent
     */
    private void raftValidateEquipInPre() {
        Event peekEvent = simulator.getEventManager().peekEvent();
        if (peekEvent instanceof CommandEvent) {
            //我们先判定这是否是一个命令事件 之后再去判定它的命令类型
            CommandEvent event = (CommandEvent) peekEvent;
            Command command = event.getCommand();
            if (command instanceof RaftOpCommand) {
                //如果是一个数据操作命令 我们才会开始记录其的变化对象 否则不记录
                RaftOpCommand raftOpCommand = (RaftOpCommand) command;
                //该节点可能会发生 数据的变化 我们将记录下这个id
                int moteId = raftOpCommand.getNodeId();
                raftDataSyncVariable.raftNeedCheckQueue.addLast(moteId);
            }
        } else if (peekEvent instanceof TransmissionEvent) {
            TransmissionEvent event = (TransmissionEvent) peekEvent;
            Mote receiver = event.getReceiver();
            if (receiver instanceof RaftMote) {
                //只有当信息的接受者是一个RaftMote的时候 才有可能会触发日志的改变
                int moteId = receiver.getMoteId();
                raftDataSyncVariable.raftNeedCheckQueue.addLast(moteId);
            }
        } else {
            new Exception("Event Type Exception").printStackTrace();
        }
    }

    /**
     * 对Raft算法的统计进行统计，
     * <p>
     * 会在Event结束后通过 RaftCalculateEventInterceptor 进行调用
     * 这里的Event必须是 TransmissionEvent 和 CommandEvent。
     * <p>
     * 依据raftValidateEquipInPre()锁定的对象，我们这里只需要对该对象的状态进行核查即可
     *
     * @see NetSimulator
     * @see RaftCalculateInPostEventInterceptor
     * @see TransmissionEvent
     * @see CommandEvent
     */
    private void raftValidateCheckInPost() {
        while (!raftDataSyncVariable.raftNeedCheckQueue.isEmpty()) {
            //首先从判定队列中 取出需要被判定的节点号
            Integer moteId = raftDataSyncVariable.raftNeedCheckQueue.pollFirst();
            Mote mote = simulator.getMoteManager().getMote(moteId);
            if (!(mote instanceof RaftMote)) {
                new Exception("该节点不是RaftMote").printStackTrace();
                return;
            }
            //我们对该节点的日志长度进行获取
            RaftMote raftMote = (RaftMote) mote;
            //取得该节点的日志表结构
            RaftLogTable table = raftMote.getLogTable();
            int length = table.getLength();
            raftDataSyncVariable.refreshLogLengthWithId(moteId, length);
        }
    }

    /**
     * 能耗统计过程
     */
    private void energyCalc() {
        MoteManager moteManager = simulator.getMoteManager();
        //为了获得每个节点的能耗数据，我们需要先对每个节点进行遍历
        for (Mote mote : moteManager.getAllMotes()) {
            //每个节点的能耗存储在自身的 EnergyStatistician 中
            EnergyStatistician energyStatistician = mote.getSingleMoteEnergyStatistician();
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


    /**
     * 共识时间对象，该类与 RaftCalculateVariable 中的相关方法绑定密切
     */
    private class ConsensusTime implements Comparable<ConsensusTime> {
        long time;
        int logIndex;

        public ConsensusTime(long time, int logIndex) {
            this.time = time;
            this.logIndex = logIndex;
        }

        @Override
        public int compareTo(ConsensusTime o) {
            return this.logIndex - o.logIndex;
        }

        @Override
        public String toString() {
            return "ConsensusTime{" +
                    "time=" + time +
                    ", logIndex=" + logIndex +
                    '}';
        }
    }

    /**
     * Raft计算会使用到的相关变量
     */
    private class RaftDataSyncVariable {
        /**
         * RaftMote 的个数
         */
        int n;
        /**
         * 该变量用来记录节点的日志长度
         * <pre>
         * {moteId, 节点日志长度}
         * </pre>
         * 所有的Mote最初默认的记录长度都是0。
         * <p>
         * 如果节点的日志长度发生了变化，我们会对这种变化进行校验。
         * <p>
         * 下标是从 [1,n] 而不是 [0,n - 1]
         */
        int[] lengthArray;

        /**
         * 记录当前值出现的次数
         * <pre>
         * {mote的日志长度, 持有该日志长度的mote个数}
         * </pre>
         */
        HashMap<Integer, Integer> lengthTimesRecorder;

        /**
         * 该变量与该类的 raftValidateEquipInPre() 和 raftValidateCheckInPost() 有密切关系
         * <p>
         * 通过 EquipInPre() 我们会往该变量中填入需要被检查的节点编号
         * <p>
         * 我们在 CheckInPost() 中，读取出这些需要被检查的节点编号，并对它们进行判定
         */
        Deque<Integer> raftNeedCheckQueue;

        /**
         * 共识达成时间列表
         * 每个元素的构成都是
         * [{1233,1},{1532,2},...]
         */
        List<ConsensusTime> consensusTimes;

        /**
         * 对raft相关指标进行统计会使用到的变量
         *
         * @param n Raft的节点个数
         */
        public RaftDataSyncVariable(int n) {
            this.n = n;
            raftNeedCheckQueue = new LinkedList<>();
            consensusTimes = new LinkedList<>();
            lengthArray = new int[n + 1];
            lengthTimesRecorder = new HashMap<>();
            // 日志长度为 0 的节点，有n个
            lengthTimesRecorder.put(0, n);
        }

        /**
         * 通过外部获取Mote的RaftLogTable的日志表，获取到节点的日志长度，
         * 使用该日志长度，刷新我们这边的记录值
         *
         * @param moteId    节点id
         * @param newLength 新的日志长度，其数值上与lastLogIndex相同
         * @see RaftMote
         * @see RaftLogTable
         */
        public void refreshLogLengthWithId(Integer moteId, int newLength) {
            int oldLength = lengthArray[moteId];
            if (oldLength != newLength) {
                //如果记录的长度发生了更新
                Integer oldLenTimes = lengthTimesRecorder.getOrDefault(oldLength, 0);
                Integer newLenTimes = lengthTimesRecorder.getOrDefault(newLength, 0);
                if (oldLenTimes == 0) {
                    new Exception("LengthRecord Exception").printStackTrace();
                    return;
                }
                //旧长度器记录器 和新长度的记录器，都将刷新
                //维护旧长度的记录器
                if (oldLenTimes - 1 > 0) {
                    //刷新记录的旧长度次数
                    lengthTimesRecorder.put(oldLength, oldLenTimes - 1);
                } else {
                    //当然，如果直接就已经为0了，我们直接删除掉该key
                    lengthTimesRecorder.remove(oldLength);
                }
                //维护新长度的记录器
                lengthTimesRecorder.put(newLength, newLenTimes + 1);
                //处理完之前的长度记录器后，开始刷新记录的长度
                lengthArray[moteId] = newLength;

                //如果某个长度的出现次数，刚好因为这种变化，达到了n个，就说明所有节点都达成了一致
                if (newLenTimes + 1 == n) {
                    allMoteConsensus(newLength);
                }
            }
        }

        /**
         * 该函数的触发条件如下：
         * <p>
         * 所有节点在某个日志长度发生变化后，达成了数据一致性，
         * 所以该函数一般被 refreshLogLengthWithId() 进行调用。
         *
         * @param newConsensusLogIndex 当前触发同步操作的日志下标，其数值上与lastLogIndex相同
         * @see RaftLogTable
         */
        private void allMoteConsensus(int newConsensusLogIndex) {
            consensusTimes.add(new ConsensusTime(simulator.getTime(), newConsensusLogIndex));
        }

    }
}

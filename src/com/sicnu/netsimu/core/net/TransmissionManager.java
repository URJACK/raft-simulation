package com.sicnu.netsimu.core.net;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.NodeAddCommand;
import com.sicnu.netsimu.core.command.NodeDelCommand;
import com.sicnu.netsimu.core.event.TransmissionEvent;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.statis.Statistician;
import com.sicnu.netsimu.core.statis.TransmitStatistician;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.netsimu.core.utils.NetSimulationRandom;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * TransmissionManager 传输管理者
 * 它需要耦合两个对象：RaftSimulator 和 Mote
 * <p>
 * 每次RaftSimulator 进行 “节点创建”、“节点删除”，
 * 都会使得TransmissionManager，对各个节点之间的关系进行重算。
 *
 * @see Mote
 * @see NodeAddCommand
 * @see NodeDelCommand
 */
@Data
public class TransmissionManager {
    /**
     * 最大传输距离（欧拉距离）
     */
    private static float MAX_TRANSMIT_DISTANCE = 200;

    /**
     * 传输速率，它的应用如下：
     * <pre>
     * 传输时延 == distance * TRANSMIT_SPEED_RATE;
     * </pre>
     */
    private static float TRANSMIT_SPEED_RATE = 0.2f;

    /**
     * 仿真器引用对象
     */
    NetSimulator simulator;

    /**
     * 零接表，每次增加节点、每次删除节点，都应当对该表进行维护。
     * <p>
     * 每个节点发送数据包的时候，不会对空间距离再做检查，只会遍历对应的邻接表
     * <pre>
     * { key = moteId, value = moteId对应的邻居表 }
     * </pre>
     */
    HashMap<Integer, LinkedList<Neighbor>> table;
    /**
     * 成功发送数据包的统计器
     * <pre>
     * {moteId, times}
     * </pre>
     */
    TransmitStatistician successSendStatistician;
    /**
     * 失败发送数据包的统计器
     * <pre>
     * {moteId, times}
     * </pre>
     */
    TransmitStatistician failedSendStatistician;
    /**
     * 成功接收数据包的统计器
     * <pre>
     * {moteId, times}
     * </pre>
     */
    TransmitStatistician successReceiveStatistician;
    /**
     * 失败接收数据包的统计器
     * <pre>
     * {moteId, times}
     * </pre>
     */
    TransmitStatistician failedReceiveStatistician;

    /**
     * @param simulator 仿真器引用
     */
    public TransmissionManager(NetSimulator simulator) {
        this.simulator = simulator;
        table = new HashMap<>();
        successSendStatistician = new TransmitStatistician();
        failedSendStatistician = new TransmitStatistician();
        successReceiveStatistician = new TransmitStatistician();
        failedReceiveStatistician = new TransmitStatistician();
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
        return (long) (distance * TRANSMIT_SPEED_RATE);
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
            Neighbor neighborOfA = new Neighbor(moteB, distance, calculateTransmitErrorRate(distance));
            Neighbor neighborOfB = new Neighbor(moteA, distance, calculateTransmitErrorRate(distance));
            neighborsOfA.add(neighborOfA);
            neighborsOfB.add(neighborOfB);
        }
    }

    /**
     * 节点发送数据包。
     * <p>
     * 这里会计算数据包失败的概率，如果失败的话，发送的数据包是不会被计入发送事件的。
     *
     * @param senderMote 发送方节点引用
     * @param packet     需要发送的数据包
     */
    public void moteSendPacket(Mote senderMote, String packet) {
        List<TransmissionManager.Neighbor> neighbors = getNeighbors(senderMote);
        //从传输管理器中 查询该节点的邻居节点
        for (TransmissionManager.Neighbor neighbor : neighbors) {
            Mote receiverMote = neighbor.getMote();
            //获取到与neighbor的距离
            float distance = neighbor.getDistance();
            //得到的随机值
            float randomVote = NetSimulationRandom.nextFloat();
            float errorRate = neighbor.getErrorRate();
            TransmissionEvent event = new TransmissionEvent(calcTransmissionTime(distance) + simulator.getTime(),
                    senderMote, receiverMote, packet);
            if (randomVote > errorRate) {
                //获取到neighbor指向的mote本身
                //无论该节点的ip和端口信息是否满足 数据包的目的地要求 我们都将其进行传输
                simulator.getEventManager().pushEvent(event);
                // 发送数据包成功后 进行数据的统计
                successSendStatistician.addValue(String.valueOf(senderMote.getMoteId()), 1);
                successReceiveStatistician.addValue(String.valueOf(receiverMote.getMoteId()), 1);
            } else {
                // 发送数据包失败后 进行数据的统计
                failedSendStatistician.addValue(String.valueOf(senderMote.getMoteId()), 1);
                failedReceiveStatistician.addValue(String.valueOf(receiverMote.getMoteId()), 1);
            }
        }
    }

    /**
     * 计算两个节点的传输失误率
     * 成功率函数建议遵照如下规则：
     * <p>
     * f(0) = 1, f(1) = 0
     * <p>
     * 失误率是通过成功率进行计算的： 这里，成功率不排除会有负数的情况。
     * 如果成功率为负数，那么失误率会 > 1。
     * 这也正好意味着发送不可能成功
     * <p>
     * 陈功率会有大于1的情况出现吗？该公式可以同样可以容忍成功率大于1的情况。
     * 当成功率大于1的时候，失误率就会 < 0
     * 那么无论如何，发送都会成功。
     * <p>
     * 正因为边界之外的情况，也能按逻辑正常运行，所以我们这里不再修饰边界变量。
     *
     * @param distance 两个节点的欧拉距离
     * @return 传输失误率
     */
    private float calculateTransmitErrorRate(float distance) {
        float x = distance / MAX_TRANSMIT_DISTANCE;
//        float successRate = (float) (Math.log(-(1.8 * distance - 2)) + 1 - Math.log(2));
        float successRate = (float) (-Math.pow(x, 8) + 1);
        return 1 - successRate;
    }

    /**
     * 邻居对象
     * 这是为了防止重复计算距离而设立的一个类
     */
    @Data
    public static class Neighbor {
        /**
         * 对应的节点引用
         */
        Mote mote;
        /**
         * 与该邻居的距离
         */
        float distance;
        /**
         * 这个距离的传输失误率
         */
        float errorRate;

        public Neighbor(Mote mote, float distance, float errorRate) {
            this.mote = mote;
            this.distance = distance;
            this.errorRate = errorRate;
        }
    }
}

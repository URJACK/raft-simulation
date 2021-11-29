package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import lombok.Data;

/**
 * “结点网络发送”命令
 */
@Data
public class NetSendCommand extends Command {
    // 节点id
    int nodeId;

    // 发送相关参数
    String srcIp;
    int srcPort;
    String dstIp;
    int dstPort;

    // 操作值类型 指明是ip还是端口
    String value;

    /**
     * @param simulator 仿真器引用对象
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     * @param srcIp     发送的源地址
     * @param srcPort   发送的源端口
     * @param dstIp     发送的目标地址
     * @param dstPort   发送的目的端口
     * @param value     发送的数值
     */
    public NetSendCommand(NetSimulator simulator, long timeStamp, String type, int nodeId,
                          String srcIp, int srcPort, String dstIp, int dstPort, String value) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
        this.value = value;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        Mote mote = moteManager.getMote(nodeId);

//        mote.netSend(new TransmissionPacket(srcIp, dstIp, srcPort, dstPort, value));
        mote.call("netSend", new TransmissionPacket(srcIp, dstIp, srcPort, dstPort, value));
    }

}

package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.net.NetStack;
import com.sicnu.netsimu.core.net.mac.MACLayer;
import com.sicnu.netsimu.raft.exception.ParseException;
import lombok.Data;

/**
 * “结点网络发送”命令
 * <pre>
 * 1000, NET_SEND, 1, EE:EE:EE:EE:EE:2, hello
 * </pre>
 * 发送的逻辑，本质上是先调用NetStack的convert方法，得到发送的数据字符串，
 * 再调用 Mote 的 netSend 方法
 *
 * @see Mote
 * @see NetStack
 */
@Data
public class NetSendCommand extends Command {
    // 节点id
    int nodeId;

    // 发送相关参数
    String dstMac;

    // 操作值类型 指明是ip还是端口
    String value;

    /**
     * @param simulator 仿真器引用对象
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     * @param dstMac    目标的MAC地址
     * @param value     发送的数值
     */
    public NetSendCommand(NetSimulator simulator, long timeStamp, String type, int nodeId,
                          String dstMac, String value) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.dstMac = dstMac;
        this.value = value;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        Mote mote = moteManager.getMote(nodeId);
        NetStack netStack = mote.getNetStack();
//        mote.netSend(new TransmissionPacket(srcIp, dstIp, srcPort, dstPort, value));
        try {
            MACLayer.Header header = new MACLayer.Header(netStack.getInfo("mac"), dstMac);
            mote.call("netSend", netStack.convert(value, header));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

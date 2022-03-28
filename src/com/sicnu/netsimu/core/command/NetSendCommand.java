package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.node.NodeManager;
import com.sicnu.netsimu.core.net.NetStack;
import com.sicnu.netsimu.core.net.mac.IEEE_802_11_MACLayer;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.netsimu.exception.ParseException;
import lombok.Data;

/**
 * “结点网络发送”命令
 * <pre>
 * 1000, NET_SEND, 1, EE:EE:EE:EE:EE:2, hello
 * </pre>
 * 发送的逻辑，本质上是先调用NetStack的convert方法，得到发送的数据字符串，
 * 再调用 Mote 的 netSend 方法
 *
 * @see Node
 * @see NetStack
 */
@Data
public class NetSendCommand extends Command {
    // 节点id
    int nodeId;

    // 发送相关参数
    byte[] dstMac;

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
        try {
            this.dstMac = MoteCalculate.convertStrAddressIntoByteAddress(dstMac);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.value = value;
    }

    @Override
    public void work() {
        NodeManager nodeManager = simulator.getMoteManager();
        Node node = nodeManager.getMote(nodeId);
        NetStack netStack = node.getNetStack();
//        mote.netSend(new TransmissionPacket(srcIp, dstIp, srcPort, dstPort, value));
        IEEE_802_11_MACLayer.Header header = IEEE_802_11_MACLayer.Header.Builder.createDataPacket(dstMac, (byte[]) netStack.getInfo("mac"));
//            mote.call("netSend", (Object) netStack.convert(value, header));
    }

}

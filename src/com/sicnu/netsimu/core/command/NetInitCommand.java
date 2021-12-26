package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.net.NetStack;
import com.sicnu.netsimu.exception.ParseException;
import lombok.Data;

/**
 * “结点网络设置”命令
 * <pre>
 *  1000, NET_INIT, 2, mac, EE:EE:EE:EE:EE:03
 * </pre>
 * 这里可以设置的字段名，需要参考相对应的NetStack中，
 * getInfo()和setInfo()可用的key值
 *
 * @see NetStack
 */
@Data
public class NetInitCommand extends Command {
    // 节点id
    int nodeId;
    /**
     * 操作值类型 指明是ip还是端口 还是mac
     */
    String operation;
    String value;

    /**
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      指令类型
     * @param nodeId    节点id
     * @param operation "mac" or "ip"
     * @param value     操作值
     */
    public NetInitCommand(NetSimulator simulator, long timeStamp, String type, int nodeId, String operation, String value) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.operation = operation;
        this.value = value;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        Mote mote = moteManager.getMote(nodeId);
        NetStack netStack = mote.getNetStack();
        try {
            netStack.setInfo(operation, value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

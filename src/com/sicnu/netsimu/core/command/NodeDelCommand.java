package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.MoteManager;
import lombok.Data;

/**
 * “节点删除”命令
 * <pre>
 * 1000, NODE_DEL, 1
 * </pre>
 * 删除“节点1”。
 *
 * @see NodeAddCommand
 */
@Data
public class NodeDelCommand extends Command {
    // 节点的id
    int nodeId;

    /**
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点的Id
     */
    public NodeDelCommand(NetSimulator simulator, long timeStamp, String type, int nodeId) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        if (moteManager.containMote(nodeId)) {
            moteManager.deleteMote(nodeId);
        }
    }
}

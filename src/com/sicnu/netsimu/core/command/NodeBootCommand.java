package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import lombok.Data;

/**
 * “启动节点”命令
 */
@Data
public class NodeBootCommand extends Command {
    // 节点id
    int nodeId;

    /**
     *
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type 命令类型
     * @param nodeId 节点id
     */
    public NodeBootCommand(NetSimulator simulator, long timeStamp, String type, int nodeId) {
        super(simulator,timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

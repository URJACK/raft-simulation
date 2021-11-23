package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import lombok.Data;

/**
 * “结点关闭”动作
 */
@Data
public class NodeShutCommand extends Command {
    int nodeId;

    public NodeShutCommand(NetSimulator simulator, long timeStamp, CommandType type, int nodeId) {
        super(simulator,timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

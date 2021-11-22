package com.sicnu.raftsimu.core.command;

import lombok.Data;

/**
 * “结点关闭”动作
 */
@Data
public class NodeShutCommand extends Command {
    int nodeId;

    public NodeShutCommand(long timeStamp, CommandType type, int nodeId) {
        super(timeStamp, type);
        this.nodeId = nodeId;
    }
}

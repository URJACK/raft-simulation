package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import lombok.Data;

/**
 * “结点关闭”动作
 */
@Data
public class NodeShutCommand extends Command {
    int nodeId;

    public NodeShutCommand(RaftSimulator simulator, long timeStamp, CommandType type, int nodeId) {
        super(simulator,timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
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
     * @param simulator
     * @param timeStamp 时间戳
     * @param type 命令类型
     * @param nodeId 节点id
     */
    public NodeBootCommand(RaftSimulator simulator, long timeStamp, CommandType type, int nodeId) {
        super(simulator,timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import lombok.Data;

/**
 * Raft选举命令
 */
@Data
public class RaftElectCommand extends Command {
    int nodeId;

    /**
     * @param simulator
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     */
    public RaftElectCommand(RaftSimulator simulator, long timeStamp, CommandType type, int nodeId) {
        super(simulator,timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

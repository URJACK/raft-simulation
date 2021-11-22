package com.sicnu.raftsimu.core.command;

import lombok.Data;

/**
 * Raft选举命令
 */
@Data
public class RaftElectCommand extends Command {
    int nodeId;

    /**
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     */
    public RaftElectCommand(long timeStamp, CommandType type, int nodeId) {
        super(timeStamp, type);
        this.nodeId = nodeId;
    }
}

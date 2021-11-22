package com.sicnu.raftsimu.core.command;

import lombok.Data;

/**
 * 心跳包
 */
@Data
public class RaftBeatCommand extends Command {
    int nodeId;

    /**
     *
     * @param timeStamp 时间戳
     * @param type 命令类型
     * @param nodeId 节点id
     */
    public RaftBeatCommand(long timeStamp, CommandType type, int nodeId) {
        super(timeStamp, type);
        this.nodeId = nodeId;
    }
}

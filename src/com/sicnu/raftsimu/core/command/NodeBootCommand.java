package com.sicnu.raftsimu.core.command;

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
     * @param timeStamp 时间戳
     * @param type 命令类型
     * @param nodeId 节点id
     */
    public NodeBootCommand(long timeStamp, CommandType type, int nodeId) {
        super(timeStamp, type);
        this.nodeId = nodeId;
    }
}

package com.sicnu.raftsimu.core.command;

import lombok.Data;

/**
 * 删除节点命令
 */
@Data
public class NodeDelCommand extends Command {
    // 节点的id
    int nodeId;

    /**
     *
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点的Id
     */
    public NodeDelCommand(long timeStamp, CommandType type, int nodeId) {
        super(timeStamp, type);
        this.nodeId = nodeId;
    }
}

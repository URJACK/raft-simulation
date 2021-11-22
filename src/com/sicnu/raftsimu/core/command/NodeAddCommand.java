package com.sicnu.raftsimu.core.command;

import lombok.Data;

/**
 * 添加节点命令
 */
@Data
public class NodeAddCommand extends Command {
    // 被添加的节点id
    int nodeId;
    // 添加节点后的位置
    float x;
    float y;

    /**
     * 添加节点操作构造函数
     * @param timeStamp 时间戳
     * @param type 命令类型
     * @param nodeId 节点id
     * @param x 节点x坐标
     * @param y 节点y坐标
     */
    public NodeAddCommand(long timeStamp, CommandType type, int nodeId, float x, float y) {
        super(timeStamp, type);
        this.nodeId = nodeId;
        this.x = x;
        this.y = y;
    }
}

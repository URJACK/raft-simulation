package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.mote.MoteManager;
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
     *
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     * @param x         节点x坐标
     * @param y         节点y坐标
     */
    public NodeAddCommand(RaftSimulator simulator, long timeStamp, CommandType type, int nodeId, float x, float y) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.x = x;
        this.y = y;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        if (!moteManager.containMote(nodeId)) {
            //如果当前节点的id已经存在 则无法进行添加节点
            moteManager.addMote(nodeId, x, y);
        }
    }
}

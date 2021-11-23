package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.trans.TransmissionManager;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
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
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     * @param x         节点x坐标
     * @param y         节点y坐标
     */
    public NodeAddCommand(NetSimulator simulator, long timeStamp, CommandType type, int nodeId, float x, float y) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.x = x;
        this.y = y;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        TransmissionManager transmissionManager = simulator.getTransmissionManager();
        if (!moteManager.containMote(nodeId)) {
            //如果当前节点的id已经存在 则无法进行添加节点
            Mote newMote = moteManager.addMote(nodeId, x, y);
            if (newMote == null) {
                //创建节点失败
                return;
            }
            //添加结点后，传输管理器需要刷新节点间的关系
            transmissionManager.addNode(nodeId);
            // 每个新结点，都将触发自身的init()函数
            newMote.init();
        }
    }
}

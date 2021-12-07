package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.net.TransmissionManager;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import lombok.Data;

/**
 * 添加节点命令
 * <pre>
 * 1000, NODE_ADD, 1, 50, 100, com.sicnu.netsimu.core.mote.NormalMote
 * 1000, NODE_ADD, 3, 100, 100, com.sicnu.raft.mote.RaftMote , 3
 * </pre>
 * 节点类型后，可以跟上额外参数字段
 * @see NodeDelCommand
 */
@Data
public class NodeAddCommand extends Command {
    String[] args;
    // 被添加的节点id
    int nodeId;
    // 添加节点后的位置
    float x;
    float y;
    // 添加的节点类型字串
    private String nodeClass;

    /**
     * 添加节点操作构造函数
     *
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     * @param x         节点x坐标
     * @param y         节点y坐标
     * @param nodeClass 节点类型
     */
    public NodeAddCommand(NetSimulator simulator, long timeStamp, String type, int nodeId, float x, float y, String nodeClass, String... args) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.x = x;
        this.y = y;
        this.nodeClass = nodeClass;
        this.args = args;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        TransmissionManager transmissionManager = simulator.getTransmissionManager();
        if (!moteManager.containMote(nodeId)) {
            //如果当前节点的id已经存在 则无法进行添加节点
            Mote newMote = moteManager.addMote(nodeId, x, y, nodeClass, args);
            if (newMote == null) {
                //创建节点失败
                return;
            }
            //添加结点后，传输管理器需要刷新节点间的关系
            transmissionManager.addNode(nodeId);
            // 每个新结点，都将触发自身的init()函数
//            newMote.init();
            newMote.call("init");
        }
    }
}

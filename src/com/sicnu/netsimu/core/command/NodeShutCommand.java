package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.node.NodeManager;
import lombok.Data;

/**
 * “节点关闭”命令
 * <pre>
 * 1000, NODE_SHUT, 1
 * </pre>
 * 关闭“节点1”。
 *
 * @see NodeBootCommand
 */
@Data
public class NodeShutCommand extends Command {
    int nodeId;

    public NodeShutCommand(NetSimulator simulator, long timeStamp, String type, int nodeId) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {
        NodeManager nodeManager = simulator.getMoteManager();
        if (nodeManager.containMote(nodeId)) {
            Node node = nodeManager.getMote(nodeId);
            if (node == null) {
                //获取节点失败
                return;
            }
            // 每个新结点，都将触发自身的shut()函数
//            mote.shut();
            node.call("shut");
        }
    }
}

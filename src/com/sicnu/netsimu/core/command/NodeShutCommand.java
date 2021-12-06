package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
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

    }
}

package com.sicnu.raft.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.Command;
import com.sicnu.raft.node.RaftNode;
import com.sicnu.raft.role.RaftRoleLogic;
import lombok.Data;

/**
 * Raft 数据操作命令
 * <p>
 * 用来触发各种Raft算法的日志的相关操作
 * 命令通过 Command -> Mote -> Role 的传递链传递动作
 * <p>
 * <pre>
 * 1200, RAFT_OP, 1, add, name, hello
 * </pre>
 *
 * @see RaftNode
 * @see RaftRoleLogic
 */
@Data
public class RaftOpCommand extends Command {
    int nodeId;
    String operation;
    String key;
    String value;

    /**
     * @param simulator 模拟器引用对象
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     * @param operation 操作类型
     * @param key       操作键
     * @param value     操作值
     */
    public RaftOpCommand(NetSimulator simulator, long timeStamp, String type, int nodeId, String operation, String key, String value) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    @Override
    public void work() {
        RaftNode mote = (RaftNode) simulator.getMoteManager().getMote(nodeId);
        mote.logOperate(operation, key, value);
    }
}

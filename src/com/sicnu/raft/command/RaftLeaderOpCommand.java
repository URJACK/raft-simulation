package com.sicnu.raft.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.Command;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.raft.node.RaftNode;
import com.sicnu.raft.role.RaftRoleLogic;
import lombok.Data;

/**
 * Raft 数据操作命令
 * <p>
 * 让Raft算法的Leader来触发调用各种Raft算法的日志的相关操作
 * <p>
 * 假设有多个Leader，这里我们是让所有的Leader还是让term最大的Leader呢？
 * 我们这里让所有的Leader，这样说不定可以测试出一些其他的Bug。
 * <p>
 * 命令通过 Command -> Mote -> Role 的传递链传递动作
 * <p>
 * <pre>
 * 1200, RAFT_LEADER_OP, add, name, hello
 * </pre>
 *
 * @see RaftNode
 * @see RaftRoleLogic
 */
@Data
public class RaftLeaderOpCommand extends Command {
    int nodeId;
    String operation;
    String key;
    String value;

    /**
     * @param simulator 模拟器引用对象
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param operation 操作类型
     * @param key       操作键
     * @param value     操作值
     */
    public RaftLeaderOpCommand(NetSimulator simulator, long timeStamp, String type, String operation, String key, String value) {
        super(simulator, timeStamp, type);
        this.operation = operation;
        this.key = key;
        this.value = value;
    }

    @Override
    public void work() {
        for (Node node : simulator.getMoteManager().getAllMotes()) {
            if (!(node instanceof RaftNode)) {
                continue;
            }
            RaftNode raftNode = (RaftNode) node;
            if (raftNode.getRole() != RaftRoleLogic.ROLE_LEADER) {
                continue;
            }
            // 所有的Leader都会执行该操作
            raftNode.logOperate(operation, key, value);
        }
    }
}

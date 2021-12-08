package com.sicnu.raft.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.Command;
import lombok.Data;

/**
 * Raft选举命令，它应当能够触发对应Raft节点的选举命令
 * <pre>
 * 1000, RAFT_ELECT, 3
 * </pre>
 * 该类目前没有做具体实现
 */
@Data
public class RaftElectCommand extends Command {
    int nodeId;

    /**
     * @param simulator
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     */
    public RaftElectCommand(NetSimulator simulator, long timeStamp, String type, int nodeId) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

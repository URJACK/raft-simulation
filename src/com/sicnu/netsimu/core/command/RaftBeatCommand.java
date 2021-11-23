package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import lombok.Data;

/**
 * 心跳包
 */
@Data
public class RaftBeatCommand extends Command {
    int nodeId;

    /**
     * @param simulator
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     */
    public RaftBeatCommand(NetSimulator simulator, long timeStamp, CommandType type, int nodeId) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {

    }
}

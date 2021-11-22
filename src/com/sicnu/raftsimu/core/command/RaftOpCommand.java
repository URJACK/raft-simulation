package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import lombok.Data;

/**
 * Raft 数据操作命令
 */
@Data
public class RaftOpCommand extends Command {
    int nodeId;
    Operation operation;
    String key;
    String value;

    @Override
    public void work() {

    }

    public enum Operation {
        ADD, DEL, MODIFY
    }

    /**
     *
     * @param simulator 模拟器引用对象
     * @param timeStamp 时间戳
     * @param type 命令类型
     * @param nodeId 节点id
     * @param operation 操作类型
     * @param key 操作键
     * @param value 操作值
     */
    public RaftOpCommand(RaftSimulator simulator, long timeStamp, CommandType type, int nodeId, Operation operation, String key, String value) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.operation = operation;
        this.key = key;
        this.value = value;
    }


}

package com.sicnu.raftsimu.core.command;

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

    public enum Operation {
        ADD, DEL, MODIFY
    }

    public RaftOpCommand(long timeStamp, CommandType type, int nodeId, Operation operation, String key, String value) {
        super(timeStamp, type);
        this.nodeId = nodeId;
        this.operation = operation;
        this.key = key;
        this.value = value;
    }


}

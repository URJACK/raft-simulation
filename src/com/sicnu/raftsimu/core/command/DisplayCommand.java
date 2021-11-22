package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import lombok.Data;

/**
 * “结点关闭”动作
 */
@Data
public class DisplayCommand extends Command {

    public DisplayCommand(RaftSimulator simulator, long timeStamp, CommandType type) {
        super(simulator, timeStamp, type);
    }

    @Override
    public void work() {

    }
}

package com.sicnu.raftsimu.core.event;

import com.sicnu.raftsimu.core.command.Command;

public class CommandEvent extends Event {
    Command command;

    public CommandEvent(long triggerTime, Command command) {
        super(triggerTime);
        this.command = command;
    }

    @Override
    public void work() {
        command.work();
    }
}

package com.sicnu.raftsimu.ui;

import com.sicnu.raftsimu.core.command.Command;

public class CommandParseException extends Exception {

    Command.CommandType commandType;

    public CommandParseException(String message) {
        super(message);
    }

    public CommandParseException(String message, Command.CommandType type) {
        super(message);
        this.commandType = type;
    }

    @Override
    public String toString() {
        return commandType == null ? "" : commandType + super.toString();
    }
}

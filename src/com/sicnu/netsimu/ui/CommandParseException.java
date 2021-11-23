package com.sicnu.netsimu.ui;

import com.sicnu.netsimu.core.command.Command;

/**
 * 命令转换异常
 */
public class CommandParseException extends Exception {
    /**
     * 所属的命令类型
     * 可能为空。
     * 是否为空决定了toString()逻辑的不同
     */
    Command.CommandType commandType;

    /**
     * @param message 异常信息
     */
    public CommandParseException(String message) {
        super(message);
    }

    /**
     * @param message 异常信息
     * @param type    可能的命令类型
     */
    public CommandParseException(String message, Command.CommandType type) {
        super(message + type.getClass().toString());
        this.commandType = type;
    }

    @Override
    public String toString() {
        return commandType == null ? super.toString() : commandType + super.toString();
    }
}

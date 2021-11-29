package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.ui.CommandParseException;

/**
 * BasicCommandTranslator 是一个用户接口类，
 * 与CommandTranslator相比，没有解析额外的指令
 * 它可以读取文本（内容是一串串的指令），并解析为若干条指令。
 * 可以作为一个成员变量被传入RaftSimulator之中，进而完成工作。
 */
public class BasicCommandTranslator extends CommandTranslator {
    /**
     * 与 extendParse 搭配使用
     * 当有新的命令集合后 可以在该函数中，对新的命令集合进行初始化
     */
    @Override
    protected void extendInit() {

    }

    /**
     * 扩展转换函数 可以转换额外的命令
     *
     * @param commandStrings 命令字符串
     * @return
     */
    @Override
    protected Command extendParse(String[] commandStrings) throws CommandParseException {
        throw new CommandParseException("基础命令无法匹配");
    }
}

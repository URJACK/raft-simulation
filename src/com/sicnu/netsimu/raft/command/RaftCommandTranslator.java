package com.sicnu.netsimu.raft.command;

import com.sicnu.netsimu.core.command.*;
import com.sicnu.netsimu.ui.CommandParseException;

import java.util.HashMap;
import java.util.HashSet;

public class RaftCommandTranslator extends CommandTranslator {
    protected HashSet<String> extendsCommandTypeHashset;
    protected HashMap<String, Integer> extendsCommandLengthHashMap;
    protected HashMap<String, RaftOpCommand.Operation> raftOperation;

    /**
     * 与 extendParse 搭配使用
     * 当有新的命令集合后 可以在该函数中，对新的命令集合进行初始化
     * <p>
     * // <用户输入的指令名, 指令的枚举类型>
     * protected final HashSet<String> basicCommandTypeHashset;
     * // <用户输入的指令名, 对应指令的参数长度>
     * protected final HashMap<String, Integer> basicCommandLengthHashMap;
     * // <用户输入的Raft操作名，操作的枚举类型>
     * protected final HashMap<String, NetInitCommand.Operation> netOperationTypeHashMap;
     */
    @Override
    protected void extendInit() {
        extendsCommandTypeHashset = new HashSet<>();
        extendsCommandLengthHashMap = new HashMap<>();
        raftOperation = new HashMap<>();
        //指令集合
        extendsCommandTypeHashset.add("RAFT_ELECT");
        extendsCommandTypeHashset.add("RAFT_BEATS");
        extendsCommandTypeHashset.add("RAFT_OP");
        //指令长度集合
        extendsCommandLengthHashMap.put("RAFT_ELECT", 3);
        extendsCommandLengthHashMap.put("RAFT_BEATS", 3);
        extendsCommandLengthHashMap.put("RAFT_OP", 6);
        //操作类型集合
        raftOperation.put("add", RaftOpCommand.Operation.ADD);
        raftOperation.put("del", RaftOpCommand.Operation.DEL);
        raftOperation.put("modify", RaftOpCommand.Operation.MODIFY);
    }

    /**
     * 扩展转换函数 可以转换额外的命令
     *
     * @param commandStrings 命令字符串
     * @return
     */
    @Override
    protected Command extendParse(String[] commandStrings) throws CommandParseException {
        long timeStamp = Long.parseLong(commandStrings[0]);
        String commandType = commandStrings[1];
        boolean result = extendsCommandTypeHashset.contains(commandType);
        if (!result) {
            throw new CommandParseException("没有找到合适的命令");
        } else {
            //读取到当前的指令参数长度
            int commandLen = extendsCommandLengthHashMap.get(commandStrings[1]);
            if (commandStrings.length != commandLen) {
                //如果指令长度 与应有长度 不匹配
                throw new CommandParseException("Command's length is not matched", commandType);
            }
            return switch (commandType) {
                case "RAFT_ELECT" ->
                        // 生成控制台展示接口
                        new RaftElectCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]));
                case "RAFT_BEATS" ->
                        // 生成控制台展示接口
                        new RaftBeatCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]));
                case "RAFT_OP" ->
                        // 生成控制台展示接口
                        new RaftOpCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]),
                                raftOperation.get(commandStrings[3]), commandStrings[4], commandStrings[5]);
                default -> throw new CommandParseException("No Matched Type");
            };
        }
    }
}

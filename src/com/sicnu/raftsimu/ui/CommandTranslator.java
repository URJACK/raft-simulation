package com.sicnu.raftsimu.ui;

import com.sicnu.raftsimu.core.command.*;

import java.io.*;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * CommandTranslator 是一个用户接口类，
 * 它可以读取文本（内容是一串串的指令），并解析为若干条指令。
 * 可以作为一个成员变量被传入RaftSimulator之中，进而完成工作。
 */
public class CommandTranslator {
    // 使用read() 后，会对该变量进行初始化

    private Deque<Command> commands;
    // <用户输入的指令名, 指令的枚举类型>

    private final HashMap<String, Command.CommandType> commandTypeHashMap;
    // <用户输入的指令名, 对应指令的参数长度>
    private final HashMap<String, Integer> commandLengthHashMap;
    // <用户输入的Raft操作名，操作的枚举类型>
    private final HashMap<String, RaftOpCommand.Operation> operationTypeHashMap;
    /**
     * 在构造函数中 我们为了能快速的生成指令 我们提前在这边构建两个hashMap
     * 使用String，分别能够获取到“指令类型”和“指令长度”
     */
    public CommandTranslator() {
        //初始化对象
        commandTypeHashMap = new HashMap<>();
        commandLengthHashMap = new HashMap<>();
        operationTypeHashMap = new HashMap<>();
        //对指令集进行初始化
        commandTypeHashMap.put("NODE_ADD", Command.CommandType.NODE_ADD);
        commandTypeHashMap.put("NODE_DEL", Command.CommandType.NODE_DEL);
        commandTypeHashMap.put("NODE_BOOT", Command.CommandType.NODE_BOOT);
        commandTypeHashMap.put("NODE_SHUT", Command.CommandType.NODE_SHUT);
        commandTypeHashMap.put("RAFT_ELECT", Command.CommandType.RAFT_ELECT);
        commandTypeHashMap.put("RAFT_BEAT", Command.CommandType.RAFT_BEAT);
        commandTypeHashMap.put("RAFT_OP", Command.CommandType.RAFT_OP);
        //对指令长度集进行初始化
        commandLengthHashMap.put("NODE_ADD", 5);
        commandLengthHashMap.put("NODE_DEL", 3);
        commandLengthHashMap.put("NODE_BOOT", 3);
        commandLengthHashMap.put("NODE_SHUT", 3);
        commandLengthHashMap.put("RAFT_ELECT", 3);
        commandLengthHashMap.put("RAFT_BEAT", 3);
        commandLengthHashMap.put("RAFT_OP", 6);
        //对数据操作集进行初始化
        operationTypeHashMap.put("add", RaftOpCommand.Operation.ADD);
        operationTypeHashMap.put("del", RaftOpCommand.Operation.DEL);
        operationTypeHashMap.put("modify", RaftOpCommand.Operation.MODIFY);
    }

    /**
     * CommandTranslator 通过读取文本，解析文本的指令信息
     * 将这些解析出来的指令序列，都存入自己的序列中。
     *
     * @param filePath 读取的文件路径
     */
    public void read(String filePath) {
        commands = new LinkedList<>();
        Command bufferCommand = null;
        File file = new File(filePath);
        try {
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String s = null;
            while ((s = br.readLine()) != null) {
                Command command = parse(s);
                long lastTime = bufferCommand == null ? 0 : bufferCommand.getTimeStamp();
                if (command.getTimeStamp() < lastTime) {
                    throw new Exception("TimeSequence exception, each commands must be in a order");
                }
                commands.addLast(command);
                bufferCommand = command;
            }
            br.close();
        } catch (Exception e) {
            commands = null;
            e.printStackTrace();
        }
    }

    /**
     * @param commandText 特定格式的文本，每一行的格式如下：
     *                    [时间戳(微秒)],[操作名],[操作号],[操作值]
     *                    1000, RAFT_OP, 1, add, x, 2
     * @return 解析得到的命令
     */
    Command parse(String commandText) throws CommandParseException {
        commandText = clearSpace(commandText);
        String[] commandStrings = commandText.split(",");
        if (commandStrings.length < 3) {
            //最短的指令，也应当有三个参数
            throw new CommandParseException("Invalid command, the parameters' num is less than 3");
        }
        long timeStamp = Long.parseLong(commandStrings[0]);
        Command.CommandType type = commandTypeHashMap.get(commandStrings[1]);
        if (type == null) {
            //没有找到合适的指令名
            throw new CommandParseException("No matched type");
        }
        //读取到当前的指令参数长度
        int commandLen = commandLengthHashMap.get(commandStrings[1]);
        if (commandStrings.length != commandLen) {
            //如果指令长度 与应有长度 不匹配
            throw new CommandParseException("Command's length is not matched", type);
        }
        //依据相应的指令塞入参数
        if (type == Command.CommandType.NODE_ADD) {
            //生成 “添加节点”
            return new NodeAddCommand(timeStamp, type, Integer.parseInt(commandStrings[2]),
                    Float.parseFloat(commandStrings[3]), Float.parseFloat(commandStrings[4]));
        } else if (type == Command.CommandType.NODE_DEL) {
            //生成“删除节点”
            return new NodeDelCommand(timeStamp, type, Integer.parseInt(commandStrings[2]));
        } else if (type == Command.CommandType.NODE_BOOT) {
            //生成“启动节点”
            return new NodeBootCommand(timeStamp, type, Integer.parseInt(commandStrings[2]));
        } else if (type == Command.CommandType.NODE_SHUT) {
            //生成“关闭节点”
            return new NodeShutCommand(timeStamp, type, Integer.parseInt(commandStrings[2]));
        } else if (type == Command.CommandType.RAFT_ELECT) {
            //生成“节点选举”
            return new RaftElectCommand(timeStamp, type, Integer.parseInt(commandStrings[2]));
        } else if (type == Command.CommandType.RAFT_BEAT) {
            //生成“节点心跳”
            return new RaftBeatCommand(timeStamp, type, Integer.parseInt(commandStrings[2]));
        } else if (type == Command.CommandType.RAFT_OP) {
            return new RaftOpCommand(timeStamp, type, Integer.parseInt(commandStrings[2]),
                    operationTypeHashMap.get(commandStrings[3]), commandStrings[4], commandStrings[5]);
        } else {
            throw new CommandParseException("No Matched Type");
        }
    }

    /**
     * 将一段用户导入的输入 清理其多余的空格
     * 方便后续解析命令
     *
     * @param commandText 单个命令的字符串
     * @return 清理完空格后的字符串
     */
    private static String clearSpace(String commandText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandText.length(); i++) {
            char c = commandText.charAt(i);
            if (c != ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public Deque<Command> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return "CommandTranslator{" +
                "commands=" + commands +
                '}';
    }
}

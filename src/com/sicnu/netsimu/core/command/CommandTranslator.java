package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.utils.StringUtils;
import com.sicnu.netsimu.ui.CommandParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * CommandTranslator 是一个用户接口类，
 * 该类包含了基础指令core.command的解析过程，详见parse()函数。
 * 同时该类保留了两个abstract方法：
 * extendInit(构造函数末尾调用) 与 extendParse(被parse调用)
 * 所有的类要拓展命令转换器必须实现这两个方法
 * 可以参照BasicCommandTranslator的写法
 * <p>
 * 它可以读取文本（内容是一串串的指令），并解析为若干条指令。
 * 可以作为一个成员变量被传入RaftSimulator之中，进而完成工作。
 */
public abstract class CommandTranslator {
    protected NetSimulator simulator;
    // 使用read() 后，会对该变量进行初始化

    protected Deque<Command> commands;
    // <用户输入的指令名, 指令的枚举类型>
    protected final HashSet<String> basicCommandTypeHashset;
    // <用户输入的指令名, 对应指令的参数长度>
    protected final HashMap<String, Integer> basicCommandLengthHashMap;

    /**
     * 在构造函数中 我们为了能快速的生成指令 我们提前在这边构建两个hashMap
     * 使用String，分别能够获取到“指令类型”和“指令长度”
     */
    public CommandTranslator() {
        //初始化对象
        basicCommandTypeHashset = new HashSet<>();
        basicCommandLengthHashMap = new HashMap<>();
        //对指令集进行初始化 ---- [所有命令必须]
        basicCommandTypeHashset.add("NODE_ADD");
        basicCommandTypeHashset.add("NODE_DEL");
        basicCommandTypeHashset.add("NODE_BOOT");
        basicCommandTypeHashset.add("NODE_SHUT");
        basicCommandTypeHashset.add("NET_INIT");
        basicCommandTypeHashset.add("NET_SEND");
        basicCommandTypeHashset.add("DISPLAY_CON");
        basicCommandTypeHashset.add("SUMMARY");
        //对指令长度集进行初始化 ---- [所有命令必须]
        basicCommandLengthHashMap.put("NODE_ADD", 6);
        basicCommandLengthHashMap.put("NODE_DEL", 3);
        basicCommandLengthHashMap.put("NODE_BOOT", 3);
        basicCommandLengthHashMap.put("NODE_SHUT", 3);
        basicCommandLengthHashMap.put("NET_INIT", 5);
        basicCommandLengthHashMap.put("NET_SEND", 5);
        basicCommandLengthHashMap.put("DISPLAY_CON", 2);
        basicCommandLengthHashMap.put("SUMMARY", 3);
        //扩展初始化
        extendInit();
    }

    /**
     * 装备simulator
     *
     * @param simulator 模拟器对象
     */
    public void equipSimulator(NetSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * CommandTranslator 通过读取文本，解析文本的指令信息
     * 将这些解析出来的指令序列，都存入自己的序列中。
     *
     * @param filePath 读取的文件路径
     */
    public void read(String filePath) {
        if (simulator == null) {
            System.out.println("WARNING: the simulator is not equipped! command will not work in the right position.");
        }
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
        commandText = StringUtils.clearRedundant(commandText);
        String[] commandStrings = commandText.split(",");
        if (commandStrings.length < 2) {
            //最短的指令，也应当有两个参数
            throw new CommandParseException("Invalid command, the parameters' num is less than 2");
        }
        long timeStamp = Long.parseLong(commandStrings[0]);
        String commandType = commandStrings[1];
        boolean result = basicCommandTypeHashset.contains(commandType);
        if (!result) {
            //没有找到在基础命令中合适的指令名
            return extendParse(commandStrings);
        } else {
            //读取到当前的指令参数长度
            int commandLen = basicCommandLengthHashMap.get(commandStrings[1]);
            if (commandStrings.length < commandLen) {
                //如果指令长度 与应有长度 不匹配
                throw new CommandParseException("Command's length is not matched", commandType);
            }

            String[] externArgs = extractExternArgs(commandStrings, commandLen);
            return switch (commandType) {
                case "NODE_ADD" ->
                        //生成 “添加节点”
                        new NodeAddCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]),
                                Float.parseFloat(commandStrings[3]), Float.parseFloat(commandStrings[4]), commandStrings[5],
                                externArgs);
                case "NODE_DEL" ->
                        //生成“删除节点”
                        new NodeDelCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]));
                case "NODE_BOOT" ->
                        //生成“启动节点”
                        new NodeBootCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]));
                case "NODE_SHUT" ->
                        //生成“关闭节点”
                        new NodeShutCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]));
                case "NET_INIT" ->
                        //生成“初始化网络”命令
                        new NetInitCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]),
                                commandStrings[3], commandStrings[4]);
                case "NET_SEND" ->
                        // 生成“网络发送”命令
                        new NetSendCommand(simulator, timeStamp, commandType, Integer.parseInt(commandStrings[2]),
                                commandStrings[3], commandStrings[4]);
                case "DISPLAY_CON" ->
                        // 生成控制台展示接口
                        new DisplayCommand(simulator, timeStamp, commandType);
                case "SUMMARY" ->
                        // 生成总结命令
                        new SummaryCommand(simulator, timeStamp, commandType, commandStrings[2]);
                default -> throw new CommandParseException("No Matched Type");
            };
        }
    }

    /**
     * 解析提取额外参数（将命令长度抛去后的所有字符串）
     *
     * @param commandStrings 原始解析命令行数组
     * @param commandLen     该命令对应的长度
     * @return 额外参数数组
     */
    protected String[] extractExternArgs(String[] commandStrings, int commandLen) {
        String[] externArgs = new String[commandStrings.length - commandLen];
        for (int i = 0; i < externArgs.length; i++) {
            externArgs[i] = commandStrings[commandLen + i];
        }
        return externArgs;
    }

    // abstract Methods //

    /**
     * 与 extendParse 搭配使用
     * 当有新的命令集合后 可以在该函数中，对新的命令集合进行初始化
     * <p>
     * // <用户输入的指令名, 指令的枚举类型>
     * private final HashSet<String> basicCommandTypeHashset;
     * // <用户输入的指令名, 对应指令的参数长度>
     * private final HashMap<String, Integer> basicCommandLengthHashMap;
     * // <用户输入的Raft操作名，操作的枚举类型>
     * private final HashMap<String, NetInitCommand.Operation> netOperationTypeHashMap;
     */
    protected abstract void extendInit();

    /**
     * 扩展转换函数 可以转换额外的命令
     *
     * @param commandStrings 命令字符串
     * @return
     */
    protected abstract Command extendParse(String[] commandStrings) throws CommandParseException;

    // Getters & Setters & ToString //

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

package com.sicnu.netsimu.ui.scene;

import com.sicnu.netsimu.annotation.AllowNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 场景：主要用途是用来创建 Commands 文件
 *
 * @see com.sicnu.netsimu.core.command.CommandTranslator
 */
public abstract class Scene {
    String filePath;
    protected ArrayList<CommandGenerator> commands;

    /**
     * @param filePath 需要被创建的 commands 文件路径
     */
    public Scene(String filePath) {
        this.filePath = filePath;
        commands = new ArrayList<>();
    }

    /**
     * 重定向文件路径
     *
     * @param filePath 文件路径
     */
    public void redirectFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 生成命令集合，为之后的output()打下基础
     *
     * @param config 生成时使用的相关配置
     */
    public abstract void generateCommands(@AllowNull Config config);

    /**
     * 配置类，每个节点在进行 generateCommands() 操作的时候，都会传入各自的Config参数
     */
    public static abstract class Config {
    }

    /**
     * 添加命令生成器对象
     * <pre>
     * //...
     * addCommandGenerator(new CommandGenerator.NodeAddCommand(linearConfig.time, i + idOffset,
     * x, y, linearConfig.nodeClass, linearConfig.args));
     * </pre>
     *
     * @param data 命令生成器对象
     */
    protected final void addCommandGenerator(CommandGenerator data) {
        commands.add(data);
    }

    /**
     * 清空已经生成的 CommandGenerator 对象
     */
    public final void clearCommandGenerators() {
        commands.clear();
    }

    /**
     * 根据自身生成的 commands， 将他们输出到 filePath 文件中。
     *
     * @param append 是否执行追加写入（如果不是第一个scene，建议填入 true）
     */
    public final void output(boolean append) {
        if (commands.size() < 1) {
            new Exception("No enough Commands to write").printStackTrace();
            return;
        }
        try {
            File file = new File(filePath);
            FileWriter fileWriter = new FileWriter(file, append);
            for (int i = 0; i < commands.size() - 1; i++) {
                String s = commands.get(i).convert();
                fileWriter.write(s);
                fileWriter.write('\n');
            }
            fileWriter.write(commands.get(commands.size() - 1).convert());
            fileWriter.close();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}

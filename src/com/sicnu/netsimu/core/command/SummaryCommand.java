package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.ui.summary.Summarizer;

/**
 * “总结指令”
 * 使用该指令可以调用Simulator的总结者进行总结
 * 3000, SUMMARY, CALC                   //传入 “CALC” 参数 适配于 IncrementalSummarizer
 * 3000, SUMMARY, OUTPUT                 //传入 “OUTPUT” 参数 适配于 IncrementalSummarizer
 */
public class SummaryCommand extends Command {

    String param;

    /**
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      指令类型
     */
    public SummaryCommand(NetSimulator simulator, long timeStamp, CommandType type, String param) {
        super(simulator, timeStamp, type);
        this.param = param;
    }

    @Override
    public void work() {
        //使用Simulator中的
        Summarizer summarizer = simulator.getSummarizer();
        if (summarizer == null) {
            System.out.println("simulator 没有装备 summarizer !!!");
            return;
        }
        summarizer.summarize(param);
    }
}

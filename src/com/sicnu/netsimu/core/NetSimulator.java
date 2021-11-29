package com.sicnu.netsimu.core;

import com.sicnu.netsimu.core.command.Command;
import com.sicnu.netsimu.core.event.EventManager;
import com.sicnu.netsimu.core.event.trans.TransmissionManager;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.ui.CommandTranslator;
import com.sicnu.netsimu.ui.InfoOutputManager;
import com.sicnu.netsimu.ui.summary.Summarizer;
import lombok.Data;

import java.util.Deque;

/**
 * 仿真核心类
 * 属于所有类的中心
 */
@Data
public class NetSimulator {
    //记录当前仿真世界的时间
    long time = 0;
    //如果超过了这个时间，仿真就结束
    long endTime = 0;
    //传输记录器 用来记录每个节点的邻居节点信息
    TransmissionManager transmissionManager;
    //节点管理器 管理结点的存储
    MoteManager moteManager;
    //事件管理器 用来快速驱动事件的推动
    EventManager eventManager;
    //信息输出管理器
    InfoOutputManager infoOutputManager;
    //总结者
    Summarizer summarizer;


    /**
     * 初始化Manager
     */
    public NetSimulator(long endTime) {
        transmissionManager = new TransmissionManager(this);
        eventManager = new EventManager(this);
        moteManager = new MoteManager(this);
        infoOutputManager = new InfoOutputManager(this);
        this.endTime = endTime;
    }

    /**
     * 装备翻译器，将翻译器得出的指令装备上
     *
     * @param translator 翻译器
     */
    public void analysis(CommandTranslator translator) {
        Deque<Command> commands = translator.getCommands();
        eventManager.analysis(commands);
    }

    /**
     * @return 当前仿真世界的时间
     */
    public long getNowTime() {
        return time;
    }

    /**
     * 模拟启动
     */
    public void run() {
        while (!eventManager.isEmpty()) {
            eventManager.exec();
            if (time > endTime) {
                //超过了这个时间，仿真就结束，不再遍历事件
                break;
            }
        }
    }

    /**
     * 模拟世界状态重置
     */
    public void reset() {

    }

    /**
     * 装备总结者
     *
     * @param summarizer 总结者引用
     */
    public void equipSummarizer(Summarizer summarizer) {
        this.summarizer = summarizer;
    }
}

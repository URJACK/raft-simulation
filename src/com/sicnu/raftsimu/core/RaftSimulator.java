package com.sicnu.raftsimu.core;

import com.sicnu.raftsimu.core.command.Command;
import com.sicnu.raftsimu.core.event.Event;
import com.sicnu.raftsimu.core.event.EventManager;
import com.sicnu.raftsimu.core.event.TransmissionEvent;
import com.sicnu.raftsimu.core.event.trans.TransmissionManager;
import com.sicnu.raftsimu.ui.CommandTranslator;
import lombok.Data;

import java.util.Deque;

/**
 * 仿真核心类
 * 属于所有类的中心
 */
@Data
public class RaftSimulator {
    //记录当前仿真世界的时间
    long time = 0;
    //传输记录器 用来记录每个节点的邻居节点信息
    TransmissionManager transmissionManager;
    //事件管理器 用来快速驱动事件的推动
    EventManager eventManager;


    public RaftSimulator(CommandTranslator translator) {
        transmissionManager = new TransmissionManager();
        eventManager = new EventManager();
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

    }

    /**
     * 模拟世界状态重置
     */
    public void reset() {

    }
}

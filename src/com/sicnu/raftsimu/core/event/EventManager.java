package com.sicnu.raftsimu.core.event;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.command.Command;

import java.util.Deque;
import java.util.PriorityQueue;

/**
 * 事件管理器
 */
public class EventManager {
    PriorityQueue<Event> queue;
    RaftSimulator simulator;

    public EventManager(RaftSimulator simulator) {
        queue = new PriorityQueue<>();
        this.simulator = simulator;
    }

    /**
     * 添加新的Event
     *
     * @param event 事件对象本身
     */
    public void pushEvent(Event event) {
        queue.add(event);
    }

    /**
     * 所有的命令序列 都可以看成是在特定时间点上触发的一个事件
     *
     * @param commands 命令序列
     */
    public void analysis(Deque<Command> commands) {
        Command command = commands.pollFirst();
        assert command != null;
        CommandEvent event = new CommandEvent(command.getTimeStamp(), command);
        queue.add(event);
    }
}

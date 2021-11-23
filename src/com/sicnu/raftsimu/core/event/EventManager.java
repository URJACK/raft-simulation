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
        while (!commands.isEmpty()) {
            Command command = commands.pollFirst();
            assert command != null;
            CommandEvent event = new CommandEvent(command.getTimeStamp(), command);
            queue.add(event);
        }
    }

    /**
     * 检查当前的Event是否为空
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * 从事件队列中，提取事件，并进行执行该事件
     * 这里需要着重检测TimeoutEvent的Loop
     */
    public void exec() {
        Event ev = queue.poll();
        assert ev != null;
        simulator.setTime(ev.getTriggerTime());
        ev.work();
        //对带loop的TimeoutEvent事件做循环处理
        if (ev instanceof TimeoutEvent) {
            TimeoutEvent timeoutEvent = (TimeoutEvent) ev;
            if (timeoutEvent.isLoop()) {
                //如果是一个循环事件 我们重算其的触发时间。
                timeoutEvent.setTriggerTime(timeoutEvent.getSpanTime() + simulator.getNowTime());
                //并将其再次加入队列
                queue.add(timeoutEvent);
            }
        }
    }
}

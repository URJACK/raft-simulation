package com.sicnu.netsimu.core.event;

import com.sicnu.netsimu.core.command.Command;

/**
 * 指令事件
 * 特指从外部传入Command对象后，转化得来的一类事件
 */
public class CommandEvent extends Event {
    //指令对象引用
    Command command;

    /**
     * @param triggerTime 事件触发时间
     * @param command     指令引用
     */
    public CommandEvent(long triggerTime, Command command) {
        super(triggerTime);
        this.command = command;
    }

    @Override
    public void work() {
        command.work();
    }
}

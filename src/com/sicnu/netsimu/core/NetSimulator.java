package com.sicnu.netsimu.core;

import com.sicnu.netsimu.core.command.Command;
import com.sicnu.netsimu.core.command.CommandTranslator;
import com.sicnu.netsimu.core.event.Event;
import com.sicnu.netsimu.core.event.EventInterceptor;
import com.sicnu.netsimu.core.event.EventManager;
import com.sicnu.netsimu.core.net.TransmissionManager;
import com.sicnu.netsimu.core.net.channel.ChannelManager;
import com.sicnu.netsimu.core.node.NodeManager;
import com.sicnu.netsimu.ui.InfoOutputManager;
import com.sicnu.netsimu.ui.summary.Summarizer;

import java.util.Deque;

/**
 * 仿真核心类
 * 属于所有类的中心
 */
public class NetSimulator {
    //记录当前仿真世界的时间
    long time = 0;
    //如果超过了这个时间，仿真就结束
    long endTime = 0;
    //传输记录器 用来记录每个节点的邻居节点信息
    TransmissionManager transmissionManager;
    //信道管理器 用来管理每个节点所处的信道状况
    ChannelManager channelManager;
    //节点管理器 管理结点的存储
    NodeManager nodeManager;
    //事件管理器 用来快速驱动事件的推动
    EventManager eventManager;
    //信息输出管理器
    InfoOutputManager infoOutputManager;
    //总结者
    Summarizer summarizer;
    //事件处理之前的 拦截器
    EventInterceptor preEventInterceptor;
    //事件处理之后的 拦截器
    EventInterceptor postEventInterceptor;


    /**
     * 初始化Manager
     */
    public NetSimulator(long endTime) {
        transmissionManager = new TransmissionManager(this);
        channelManager = new ChannelManager(this);
        eventManager = new EventManager(this);
        nodeManager = new NodeManager(this);
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
     * 模拟启动
     */
    public void run() {
        while (!eventManager.isEmpty()) {
            Event event = eventManager.peekEvent();
            if (preEventInterceptor != null) {
                preEventInterceptor.work(event);
            }
            // 执行一个离散事件 并刷新自身记录的仿真世界中的时间
            eventManager.exec();
            if (postEventInterceptor != null) {
                postEventInterceptor.work(event);
            }
            // 每当触发一个事件 就开始输出信息+
            getInfoOutputManager().outputInfo();

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

    // setters & getters //

    /**
     * @return 当前仿真世界的时间
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public TransmissionManager getTransmissionManager() {
        return transmissionManager;
    }

    public NodeManager getMoteManager() {
        return nodeManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public InfoOutputManager getInfoOutputManager() {
        return infoOutputManager;
    }

    public Summarizer getSummarizer() {
        return summarizer;
    }

    public void setPreEventInterceptor(EventInterceptor preEventInterceptor) {
        this.preEventInterceptor = preEventInterceptor;
    }

    public void setPostEventInterceptor(EventInterceptor postEventInterceptor) {
        this.postEventInterceptor = postEventInterceptor;
    }

}

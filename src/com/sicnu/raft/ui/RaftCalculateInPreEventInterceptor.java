package com.sicnu.raft.ui;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.CommandEvent;
import com.sicnu.netsimu.core.event.Event;
import com.sicnu.netsimu.core.event.EventInterceptor;
import com.sicnu.netsimu.core.event.TransmissionEvent;
import com.sicnu.netsimu.ui.summary.Summarizer;

/**
 * Raft相关指标计算类之一
 * <p>
 * 事件开始之前，对应的Summarizer 会缓存下可能操作的节点对象
 */
public class RaftCalculateInPreEventInterceptor extends EventInterceptor {
    RaftSummarizer summarizer;

    public RaftCalculateInPreEventInterceptor(NetSimulator simulator, Summarizer summarizer) throws Exception {
        super(simulator);
        if (summarizer instanceof RaftSummarizer) {
            this.summarizer = (RaftSummarizer) summarizer;
        } else {
            throw new Exception("Summarizer Type Error");
        }
    }


    @Override
    public void work(Event event) {
        if (event instanceof TransmissionEvent || event instanceof CommandEvent) {
            // 如果是传输事件、或者是外部命令，才有可能导致 Raft的日志 发生变化
            summarizer.summarize(RaftSummarizer.SYNC_PRE);
        }
    }
}

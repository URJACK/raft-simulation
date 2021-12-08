package com.sicnu.raft.ui;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.*;
import com.sicnu.netsimu.ui.summary.Summarizer;
import com.sicnu.raft.role.BasicRaftRole;

/**
 * Raft相关指标计算类之一
 * <p>
 * 事件开始之前，对应的Summarizer 会缓存下可能操作的节点对象
 */
public class RaftCalculateInPreEventInterceptor extends EventInterceptor {
    RaftSummarizer summarizer;

    public RaftCalculateInPreEventInterceptor(NetSimulator simulator, RaftSummarizer summarizer) throws Exception {
        super(simulator);
        this.summarizer = summarizer;
    }


    @Override
    public void work(Event event) {
        if (event instanceof TransmissionEvent || event instanceof CommandEvent || event instanceof BasicRaftRole.ElectionTimeoutEvent) {
            // 如果是传输事件、或者是外部命令，才有可能导致 Raft的日志 发生变化
            summarizer.summarize(RaftSummarizer.RAFT_PRE);
        }
    }
}

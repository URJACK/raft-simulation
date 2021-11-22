package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import lombok.Data;

/**
 * 指令类
 * 指令的分类大致有如下几类指令：
 * 1·节点管理类指令
 * 添加节点、删除节点、
 * 1531, NODE_ADD, 1, 45.15, 569.6      创建 id 为 1的节点，在(45.15, 569.6)这个位置上
 * 2531, NODE_DEL, 1                    删除 id 为 1的节点
 * 2·节点操作类指令
 * 启动节点、关闭节点、竞选、发送心跳包、新增数据。
 * 4512, NODE_SHUT, 1                   关闭 id 为 1的节点 (会损失自己当前的角色)
 * 4531, NODE_BOOT, 1                 	启动 id 为 1的节点 (以Follower的角色启动)
 * 5536, RAFT_ELECT, 1                	id 为 1的节点，开始执行竞选操作 (任何角色均可行)
 * 6231, RAFT_BEAT, 1                   id 为 1的节点，开始发送心跳包 (Leader 可行)
 * 7231, RAFT_OP, 1, add, x, 2          id 为 1的节点，开始进行数据操作 (Leader 可行)
 */
@Data
public abstract class Command {
    protected RaftSimulator simulator;
    protected long timeStamp;
    protected CommandType type;

    public Command() {

    }

    public Command(RaftSimulator simulator, long timeStamp, CommandType type) {
        this.timeStamp = timeStamp;
        this.type = type;
        this.simulator = simulator;
    }

    public abstract void work();

    /**
     * Command类型
     * 有三个地方是耦合的：
     * 第一个是此处的CommandType
     * 第二个是 CommandTranslator 中的 构造函数
     * 第三个是 CommandTranslator 中的 parse()
     */
    public enum CommandType {
        NODE_ADD, NODE_DEL, NODE_BOOT, NODE_SHUT, RAFT_ELECT, RAFT_BEAT, RAFT_OP, NET_INIT, NET_SEND, DISPLAY_CON
    }

}

package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.raft.mote.RaftMote;

/**
 * Raft角色
 * 所有的Raft核心代码都在该类及实现类中进行编写
 */
public abstract class RaftRole {
    /**
     * Raft节点引用
     */
    protected RaftMote mote;
    /**
     * 当前节点的角色
     */
    protected int role;
    /**
     * 当前节点的个数
     */
    protected int NODE_NUM;
    /**
     * 角色FOLLOWER，通过选举 TIMER_ELECT() 来变为 CANDIDATE
     * 之后在每次接受到数据包，会检测自己是否能变为LEADER
     * 选举应当使用创建一个终止事件的方式来检测，选举如果超时的失败情况，此时会退化为FOLLOWER。
     */
    public static final int ROLE_FOLLOWER = 0;
    /**
     * 角色CANDIDATE，选举票如果过半则变为LEADER
     * 传统模式下，如果选举超时，退化为FOLLOWER。
     */
    public static final int ROLE_CANDIDATE = 1;
    /**
     * 角色LEADER，从CANDIDATE演变而来
     * 传统模式下，如果接受到比自己term更高的LEADER，也会退化为FOLLOWER
     */
    public static final int ROLE_LEADER = 2;

    /**
     * NODE_NUM会在RaftRole被初始化
     * role变量，没有在RaftRole中进行初始化
     *
     * @param mote    所属的节点引用
     * @param nodeNum Raft节点个数
     */
    public RaftRole(RaftMote mote, int nodeNum) {
        this.mote = mote;
        this.NODE_NUM = nodeNum;
    }

    /**
     * 选举动作触发
     */
    public abstract void TIMER_ELECT();

    /**
     * Leader发送心跳包
     */
    public abstract void TIMER_BEATS();

    /**
     * 处理数据包
     * 数据包的内容，应当是一个RPC
     *
     * @param packet 数据包
     */
    public abstract void handlePacket(TransmissionPacket packet);
}

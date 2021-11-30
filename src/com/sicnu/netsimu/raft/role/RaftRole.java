package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;

public abstract class RaftRole {
    protected Mote mote;
    // 当前节点的角色
    protected int role;
    // 当前节点的个数
    protected int NODE_NUM;
    public static int ROLE_FOLLOWER = 0;
    public static int ROLE_CANDIDATE = 1;
    public static int ROLE_LEADER = 2;

    /**
     * NODE_NUM会在RaftRole被初始化
     * role变量，没有在RaftRole中进行初始化
     *
     * @param mote    所属的节点引用
     * @param nodeNum Raft节点个数
     */
    public RaftRole(Mote mote, int nodeNum) {
        this.mote = mote;
        this.NODE_NUM = nodeNum;
    }

    /**
     * 选举动作触发
     */
    public abstract void TIMER_ELECT();

    /**
     * 处理数据包
     * 数据包的内容，应当是一个RPC
     *
     * @param packet 数据包
     */
    public abstract void handlePacket(TransmissionPacket packet);
}

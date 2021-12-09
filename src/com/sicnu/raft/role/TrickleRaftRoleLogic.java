package com.sicnu.raft.role;

import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.raft.log.RaftLogTable;
import com.sicnu.raft.mote.RaftMote;

import java.util.List;

/**
 * 涓流算法逻辑
 */
public class TrickleRaftRoleLogic extends BasicRaftRoleLogic {
    /**
     * NODE_NUM会在RaftRole被初始化
     * role变量，没有在RaftRole中进行初始化
     *
     * @param mote
     * @param nodeNum Raft节点个数
     */
    public TrickleRaftRoleLogic(RaftMote mote, int nodeNum) {
        super(mote, nodeNum);
    }
}

package com.sicnu.raft.role;

import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.raft.log.RaftLogTable;
import com.sicnu.raft.mote.RaftMote;

import java.util.List;

/**
 * 涓流算法逻辑，基础逻辑相同，主要有三个地方需要进行修改。
 * <pre>
 * 1` 心跳包控制机制
 * 2` 等级控制机制 -- 无RPL 协议栈支持
 * 3` 选举缓存机制
 * </pre>
 *
 * @see BasicRaftRoleLogic
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

    @Override
    protected void initVariable(int nodeNum) {
        constantVariable = new TrickleConstantVariable();
        leaderVariable = new LeaderVariable(nodeNum);
        candidateVariable = new CandidateVariable();
    }

    protected class TrickleConstantVariable extends BasicRaftRoleLogic.ConstantVariable {
        /**
         * lazy变量用来指代时间触发间隔的翻倍值，
         * lazy = 1 : 意味着当前触发时间不会翻倍
         * <p>
         * lazy的取值范围如下：
         * <pre>
         * {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048}
         * </pre>
         */
        int lazy;

        public TrickleConstantVariable() {
            super();
            lazy = 1;
        }

        /**
         * 不使用super获取到
         */
        @Override
        protected void refreshElectionActionTime() {

        }
    }
}

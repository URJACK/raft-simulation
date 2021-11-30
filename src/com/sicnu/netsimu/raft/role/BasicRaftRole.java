package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.raft.mote.RaftMote;
import com.sicnu.netsimu.raft.rpc.ElectionRPC;
import com.sicnu.netsimu.raft.rpc.RPC;
import com.sicnu.netsimu.raft.rpc.RPCConvert;

public class BasicRaftRole extends RaftRole {

    /**
     * RaftTable raft操作表
     */
    RaftLogTable raftLogTable;
    /**
     * Leader 使用的属性
     */
    LeaderVariable leaderVariable;
    /**
     * 恒定使用属性
     */
    ConstantVariable constantVariable;
    /**
     * 候选人使用属性
     */
    CandidateVariable candidateVariable;

    /**
     * 超时选举时间 只有两个动作时间大于这个时间的时候，才能触发选举动作
     */
    long ELECT_SPAN_TIME = 500;

    /**
     * NODE_NUM会在RaftRole被初始化
     * role变量，没有在RaftRole中进行初始化
     *
     * @param nodeNum Raft节点个数
     */
    public BasicRaftRole(Mote mote, int nodeNum) {
        super(mote, nodeNum);
        // 设定节点为跟随者
        role = ROLE_FOLLOWER;
        raftLogTable = new RaftLogTable();
        // 设置当前的任期、已经投给的Candidate、当前所属的Leader
        constantVariable = new ConstantVariable(mote.getSimulator().getTime());
        // 初始化Leader使用的变量
        leaderVariable = new LeaderVariable(nodeNum);
        // 初始化Candidate使用的变量
        candidateVariable = new CandidateVariable();
    }

    // TIMER 系列 <接口> //

    /**
     * 选举动作触发
     */
    @Override
    public void TIMER_ELECT() {
        if (!shouldElectCheck()) {
            return;
        }
        //重置一下动作时间
        refreshActionTime();
        //选举之前，清空一些状态变量
        constantVariable.clearVotedAndLeader();
        candidateVariable.clear();
        mote.print("DEBUG BasicRaftRole RPC_ELECT");
        //记录的Term增加
        constantVariable.currentTerm++;
        //角色切换为 Candidate
        role = ROLE_CANDIDATE;
        //投给自己
        constantVariable.votedFor = mote.getMoteId();
        candidateVariable.hasGotVote++;
        //检查仅投给自己是否可以成为Leader
        if (candidateVariable.hasGotVote > NODE_NUM / 2) {
            TO_LEADER();
        }
        //开始广播这条RPC_ELECT消息
        broadCast(new ElectionRPC(RPC.RPC_ELECT, mote.getMoteId(),
                raftLogTable.getLastLogIndex(), raftLogTable.getLastLogTerm()));
    }

    // 抽象动作 //

    /**
     * 处理数据包
     * 数据包的内容，应当是一个RPC
     *
     * @param packet 数据包
     */
    @Override
    public void handlePacket(TransmissionPacket packet) {
        String data = packet.getData();
        char rpcType = data.charAt(0);
        switch (rpcType) {
            case RPC.RPC_ELECT:
                receiveRPCElect(packet);
                break;
            case RPC.RPC_ELECT_RESP:
                receiveRPCElectResp(packet);
                break;
            case RPC.RPC_HEARTBEATS:
                receiveRPCHeartBeats(packet);
                break;
            case RPC.RPC_HEARTBEATS_RESP:
                receiveRPCHeartBeatsResp(packet);
                break;
            default:
                //没有可以处理的
        }
    }

    // 4个不同数据包的处理转发函数 //

    private void receiveRPCHeartBeatsResp(TransmissionPacket packet) {

    }

    private void receiveRPCHeartBeats(TransmissionPacket packet) {

    }

    private void receiveRPCElectResp(TransmissionPacket packet) {

    }

    /**
     * 接收到一个数据包
     *
     * @param packet 数据包
     */
    private void receiveRPCElect(TransmissionPacket packet) {
        String data = packet.getData();
        ElectionRPC rpc = new ElectionRPC(data);
        mote.print(rpc.toString());
    }

    // 节点内部接口 //

    /**
     * 依据 mote.simulator 中的时间
     * 进而重置 constantVariable 中的动作时间
     */
    private void refreshActionTime() {
        //重新记录下动作时间
        constantVariable.lastActionTime = mote.getSimulator().getTime();
    }

    /**
     * 是否有权力执行 选举检查
     *
     * @return true == 可以进行选举
     */
    private boolean shouldElectCheck() {
        long time = mote.getSimulator().getTime();
        long lastActionTime = constantVariable.lastActionTime;
        //如果超时了 此时才可以进行选举
        if (time - lastActionTime >= ELECT_SPAN_TIME) {
            return true;
        }
        return false;
    }

    /**
     * 成为 Leader
     */
    private void TO_LEADER() {
        mote.print("I become the leader");
    }

    /**
     * 广播一个 RPC对象
     *
     * @param convert 实现了RPC对象转换接口的对象
     */
    private void broadCast(RPCConvert convert) {
        for (int i = 0; i < NODE_NUM; i++) {
            if (mote.getMoteId() - 1 == i) {
                //不会发给自己
                continue;
            }
            String dstIp = RaftUtils.getIpStr(RaftMote.IP_PREFIX, i + 1);
            TransmissionPacket packet = new TransmissionPacket(mote.getAddress(0), dstIp,
                    RaftMote.RAFT_PORT, RaftMote.RAFT_PORT, convert.convert());
            mote.netSend(packet);
        }
    }

    // 内部类定义部分 //

    class LeaderVariable {
        /**
         * 下一个下标
         */
        int[] nextIndexes;
        /**
         * 匹配的下标
         */
        int[] matchIndexes;
        /**
         * 投票的下标
         */
        int[] voteGranted;

        public LeaderVariable(int n) {
            nextIndexes = new int[n + 1];
            matchIndexes = new int[n + 1];
            voteGranted = new int[n + 1];
        }
    }

    class CandidateVariable {
        /**
         * 作为一个Candidate 取得的vote数量
         */
        int hasGotVote;

        public CandidateVariable() {
            hasGotVote = 0;
        }

        public void clear() {
            hasGotVote = 0;
        }
    }

    class ConstantVariable {
        /**
         * 当前节点的任期 默认从0 开始
         */
        int currentTerm;
        /**
         * 当前投票给了哪个Candidate
         * 默认是0，代表没有投
         */
        int votedFor;
        /**
         * 当前记录的领导Id
         * 默认是0，代表没有接受到Leader
         */
        int currentLeaderId;
        /**
         * 上次的动作时间
         * 接受到其他RPC会刷新该时间
         */
        long lastActionTime;

        public ConstantVariable(long time) {
            currentTerm = 0;
            votedFor = 0;
            currentLeaderId = 0;
            lastActionTime = time;
        }

        /**
         * 清空 VoteFor 和 currentLeaderId
         * currentTerm保留
         */
        public void clearVotedAndLeader() {
            votedFor = 0;
            currentLeaderId = 0;
        }
    }

}

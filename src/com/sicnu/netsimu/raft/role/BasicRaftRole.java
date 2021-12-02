package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.raft.RaftUtils;
import com.sicnu.netsimu.raft.annotation.NotNull;
import com.sicnu.netsimu.raft.exception.ParameterException;
import com.sicnu.netsimu.raft.mote.RaftMote;
import com.sicnu.netsimu.raft.role.log.RaftLogTable;
import com.sicnu.netsimu.raft.role.rpc.*;

/**
 * 基础Raft算法实现
 */
public class BasicRaftRole extends RaftRole {

    /**
     * Raft数据包的发送者
     */
    RaftSender raftSender;
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
     * 最大超时选举时间
     * 动作时间actionTime 会基于 mote.getTime() + random(ELECT_SPAN_TIME) 进行设置
     */
    static final long MAX_ELECT_SPAN_TIME = 500;
    /**
     * 超时选举时间浮动率
     */
    static final float MAX_ELECT_SPAN_TIME_FLOAT = 0.1f;

    /**
     * 选举最长持续时间
     * 如果超过该时长，还未收集到足够的选票，对于本人来说就选举失败了
     */
    static final long MAX_CANDIDATE_HOLD_ON_SPAN_TIME = 300;

    /**
     * NODE_NUM会在RaftRole被初始化
     * role变量，没有在RaftRole中进行初始化
     *
     * @param nodeNum Raft节点个数
     */
    public BasicRaftRole(RaftMote mote, int nodeNum) {
        super(mote, nodeNum);
        // 设定节点为跟随者
        role = ROLE_FOLLOWER;
        raftLogTable = new RaftLogTable();
        raftSender = new RaftSender(mote, NODE_NUM);
        // 设置当前的任期、已经投给的Candidate、当前所属的Leader
        constantVariable = new ConstantVariable();
        // 初始化Leader使用的变量
        leaderVariable = new LeaderVariable(nodeNum);
        // 初始化Candidate使用的变量
        candidateVariable = new CandidateVariable();
    }

    // 抽象动作 //

    /**
     * 外部时钟调用函数
     * 选举动作触发
     */
    @Override
    public void TIMER_ELECT() {
        if (!shouldElectCheck()) {
            return;
        }
        // 重置一下动作时间
        constantVariable.refreshActionTime();
        // 选举之前，清空一些状态变量
        constantVariable.clearVotedAndLeader();
        // 开始竞选之前，对参选变量进行清空
        candidateVariable.clearVoteAndActionTime();
        mote.print("DEBUG BasicRaftRole RPC_ELECT");
        // 记录的Term增加
        constantVariable.currentTerm++;
        // 角色切换为 Candidate
        role = ROLE_CANDIDATE;
        // 投给自己
        constantVariable.votedFor = mote.getMoteId();
        candidateVariable.gotVoteNum++;
        // 检查仅投给自己是否可以成为Leader
        if (candidateVariable.hasGotEnoughVoteToBeLeader()) {
            leaderVariable.TO_LEADER();
            return;
        }
        // 如果仅投给自己无法成为Leader，那么开始广播这条RPC_ELECT消息
        raftSender.broadCast(new ElectionRPC(RPC.RPC_ELECT, constantVariable.currentTerm, mote.getMoteId(),
                raftLogTable.getLastLogIndex(), raftLogTable.getLastLogTerm()));
        // 设置一个选举动作的触发时间
        mote.setTimeout(new TimeoutEvent(MAX_CANDIDATE_HOLD_ON_SPAN_TIME, false,
                mote.getSimulator(), mote) {
            @Override
            public void work() {
                // 选举超时结束动作
                candidateVariable.candidateElectionEnding();
            }
        });
    }

    /**
     * 处理数据包
     * 数据包的内容，应当是一个RPC
     *
     * @param packet 数据包
     */
    @Override
    public void handlePacket(TransmissionPacket packet) {
        String data = packet.getData();
        //从数据包中取得第一个条数据 --- Raft数据包 标识
        int rpcType = RaftUtils.getFirstValFromString(data);
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

    /**
     * 接受到心跳包的回复
     *
     * @param packet 数据包（心跳包）
     */
    private void receiveRPCHeartBeatsResp(TransmissionPacket packet) {

    }

    /**
     * 接受到心跳包 响应数据包
     *
     * @param packet
     */
    private void receiveRPCHeartBeats(TransmissionPacket packet) {

    }

    /**
     * 接受到了选举数据包的 响应数据包
     *
     * @param packet
     */
    private void receiveRPCElectResp(TransmissionPacket packet) {
        String data = packet.getData();
        ElectionRespRPC rpc = new ElectionRespRPC(data);
        if (rpc.getVoteGranted() == 1) {
            //查看是否获得选票
            candidateVariable.gotVoteNum++;
        }
        if (candidateVariable.hasGotEnoughVoteToBeLeader()) {
            //选票过半，成为Leader
            leaderVariable.TO_LEADER();
        }
    }

    /**
     * 接收到一个选举数据包
     * 会触发动作计时刷新
     *
     * @param packet 数据包
     */
    private void receiveRPCElect(TransmissionPacket packet) {
        String data = packet.getData();
        ElectionRPC rpc = new ElectionRPC(data);
        //检查RPC是否满足条件
        boolean result = electRPCValidateChecking(rpc);
        if (result) {
            //term、log 都进行了检查后，就可以进行角色变换
            try {
                // 传入的是 ElectionRPC 这会让用户进入选举态 直接改变了用户的 term
                // 之后传入 HeartbeatsRPC 将不会再改变用户的term 仅做刷新用
                constantVariable.TO_FOLLOWER(rpc.getTerm(), rpc);
                // 回发选票
                ElectionRespRPC respRpc = new ElectionRespRPC(RPC.RPC_ELECT_RESP,
                        constantVariable.currentTerm, 1, mote.getMoteId());
                raftSender.uniCast(rpc.getSenderId(), respRpc);
                // 有效操作后 重置一下动作时间
                constantVariable.refreshActionTime();
            } catch (ParameterException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 节点内部接口 //

    /**
     * 检查该选举RPC是否满足要求
     *
     * @param rpc 选举RPC
     * @return 7
     */
    private boolean electRPCValidateChecking(ElectionRPC rpc) {
        if (rpc.getTerm() <= constantVariable.currentTerm) {
            //如果目标的term如果没有比自己更高，则会无视本次选举请求。
            return false;
        }
        // 检查目标对象的日志 -- term 字段
        if (rpc.getLastLogTerm() < raftLogTable.getLastLogTerm()) {
            return false;
        }
        // 检查目标对象的日志 -- index 字段
        if (rpc.getLastLogIndex() < raftLogTable.getLastLogIndex()) {
            return false;
        }
        // 检查自己的票据，如果已经投出，返回false
        if (constantVariable.votedFor != 0) {
            return false;
        }
        return true;
    }

    /**
     * 是否有权力执行 选举检查
     *
     * @return true == 可以进行选举
     */
    private boolean shouldElectCheck() {
        long time = mote.getSimulator().getTime();
        //如果超时了 此时才可以进行选举
        if (time > constantVariable.electActionLimitTime) {
            return true;
        }
        return false;
    }

    // 内部类定义部分 //

    /**
     * Leader 会使用到的变量
     * nextIndexes、matchIndexes、voteGranted
     */
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

        /**
         * @param n Raft节点个数
         */
        public LeaderVariable(int n) {
            nextIndexes = new int[n + 1];
            matchIndexes = new int[n + 1];
            voteGranted = new int[n + 1];
        }


        /**
         * 成为 Leader
         */
        public void TO_LEADER() {
            role = ROLE_LEADER;
            //成为领导，对参选时的变量进行清空
            candidateVariable.clearVoteAndActionTime();
            constantVariable.clearVotedAndLeader();
            mote.print("I become the leader");
        }
    }

    /**
     * hasGotVote 自己作为Candidate，取得的选票数量
     * candidateActionLimitTime 候选动作终止时间
     */
    class CandidateVariable {
        /**
         * 作为一个Candidate 取得的vote数量
         */
        int gotVoteNum;
        /**
         * Candidate的触发限制时长，如果超过了这个时间，选举就失败了
         */
        long candidateActionLimitTime;

        public CandidateVariable() {
            gotVoteNum = 0;
            candidateActionLimitTime = 0;
        }

        /**
         * 清除getVoteNum和candidateActionLimitTime
         * 这两个变量
         */
        public void clearVoteAndActionTime() {
            gotVoteNum = 0;
            candidateActionLimitTime = 0;
        }

        /**
         * 选举超时结束触发动作
         * 会根据当前的role变量来做出相应的决定
         * 只有当role == LEADER 的时候，才算选举成功
         */
        public void candidateElectionEnding() {
            switch (role) {
                case ROLE_CANDIDATE:
                    this.clearVoteAndActionTime();
                    role = ROLE_FOLLOWER;
                    mote.print("选票不够，选举失败");
                    break;
                case ROLE_LEADER:
                    mote.print("选举成功");
                    break;
                case ROLE_FOLLOWER:
                    this.clearVoteAndActionTime();
                    mote.print("因为其他人，选举失败");
                    break;
                default:
            }
        }

        /**
         * 检查是否取得了足够多选票，从而成为Leader
         *
         * @return true == 可以成为 Leader
         */
        public boolean hasGotEnoughVoteToBeLeader() {
            return gotVoteNum > NODE_NUM / 2;
        }
    }

    /**
     * currentTerm
     * votedFor
     * currentLeaderId
     * lastActionTime
     */
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
         * 动作限制时间
         * 超过该时间，才有机会触发选举动作
         * <p>
         * 接受到其他RPC会刷新该时间
         */
        long electActionLimitTime;

        /**
         * 依据 mote.getSimulator().getTime()
         * 对动作时间进行初始化
         * 同时对 currentTerm、votedFor、currentLeaderId都进行初始化
         */
        public ConstantVariable() {
            currentTerm = 0;
            votedFor = 0;
            currentLeaderId = 0;
            refreshActionTime();
        }

        /**
         * 清空 VoteFor 和 currentLeaderId
         * currentTerm保留
         */
        public void clearVotedAndLeader() {
            votedFor = 0;
            currentLeaderId = 0;
        }

        /**
         * 依据 mote.simulator 中的时间
         * 进而重置 constantVariable 中的动作时间
         */
        private void refreshActionTime() {
            //重新记录下动作时间
            electActionLimitTime = mote.getSimulator().getTime() +
                    RaftUtils.floatValue(MAX_ELECT_SPAN_TIME, MAX_ELECT_SPAN_TIME_FLOAT);
        }

        /**
         * 之前已经对rpc的term 进行了诸多校验
         * term, log , votedFor
         * <p>
         * 首先改变 constantVariable.currentTerm 的变量值
         * RPC 用于改变 votedFor 或者 currentLeaderId
         *
         * @param term 新的学期数
         * @param rpc  @NotNull 接受到的RPC
         * @throws ParameterException     RPC不能传入空
         * @throws ClassNotFoundException RPC没有找到对应的实现类
         */
        public void TO_FOLLOWER(int term, @NotNull RequestRPC rpc) throws ParameterException, ClassNotFoundException {
            if (rpc == null) {
                throw new ParameterException("ElectionRPC 不能为空");
            }
            role = ROLE_FOLLOWER;
            constantVariable.currentTerm = term;
            // 清空 voted 和 currentLeaderId 记录
            // voted 和 currentLeaderId 必然有一个为空
            constantVariable.clearVotedAndLeader();
            // 在参选过程中 因为其他原因变为 Follower
            // 需要注意的是 即便在这里不使用 clear 也会最终触发 candidateElectionEnding
            candidateVariable.clearVoteAndActionTime();
            if (rpc instanceof ElectionRPC) {
                //如果是一个选举请求 自身会把votedFor进行设置
                int senderId = rpc.getSenderId();
                constantVariable.votedFor = senderId;
            } else if (rpc instanceof HeartbeatsRPC) {
                int senderId = rpc.getSenderId();
                constantVariable.currentLeaderId = senderId;
            } else {
                throw new ClassNotFoundException("没有找到合适的类");
            }
            mote.print("I become the follower ( term is " + term + " , leader is: " + rpc.getSenderId() + " )");
        }

        /**
         * 每个节点成为Follower的时候，不一定有Leader
         * 因为节点会因为其他节点的RPC-ELECT，而变为Follower
         *
         * @return true == 代表自己有Leader
         */
        public boolean hasGotLeader() {
            return currentLeaderId != 0;
        }

    }
}
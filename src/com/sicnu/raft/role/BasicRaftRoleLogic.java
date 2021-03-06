package com.sicnu.raft.role;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.raft.RaftUtils;
import com.sicnu.netsimu.annotation.NotNull;
import com.sicnu.raft.command.RaftOpCommand;
import com.sicnu.netsimu.exception.ParameterException;
import com.sicnu.netsimu.exception.RaftRuntimeException;
import com.sicnu.raft.node.RaftNode;
import com.sicnu.raft.log.RaftLogItem;
import com.sicnu.raft.log.RaftLogTable;
import com.sicnu.raft.role.rpc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基础Raft算法实现
 */
public class BasicRaftRoleLogic extends RaftRoleLogic {

    /**
     * Raft数据包的发送者
     */
    protected RaftSender raftSender;
    /**
     * RaftTable raft操作表
     */
    protected RaftLogTable raftLogTable;
    /**
     * Leader 使用的属性
     */
    protected LeaderVariable leaderVariable;
    /**
     * 恒定使用属性
     */
    protected ConstantVariable constantVariable;
    /**
     * 候选人使用属性
     */
    protected CandidateVariable candidateVariable;

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
    public BasicRaftRoleLogic(RaftNode mote, int nodeNum) {
        super(mote, nodeNum);
        // 设定节点为跟随者
        role = ROLE_FOLLOWER;
        raftLogTable = new RaftLogTable();
        raftSender = new RaftSender(mote, NODE_NUM);
        initVariable(nodeNum);
    }

    protected void initVariable(int nodeNum) {
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
        if (!constantVariable.shouldElectCheck()) {
            return;
        }
        // 重置一下动作时间
        constantVariable.refreshElectionActionTime();
        // 选举之前，清空一些状态变量
        constantVariable.clearVotedAndLeader();
        // 开始竞选之前，对参选变量进行清空
        candidateVariable.clearVoteAndActionTime();
        // 记录的Term增加
        constantVariable.currentTerm++;
        // 角色切换为 Candidate
        role = ROLE_CANDIDATE;
        // 投给自己
        constantVariable.votedFor = mote.getNodeId();
        candidateVariable.gotVoteNum++;
        // 检查仅投给自己是否可以成为Leader
        if (candidateVariable.hasGotEnoughVoteToBeLeader()) {
            leaderVariable.TO_LEADER();
            return;
        }
        // 如果仅投给自己无法成为Leader，那么开始广播这条RPC_ELECT消息
        raftSender.broadCast(new ElectionRPC(RPC.RPC_ELECT, constantVariable.currentTerm, mote.getNodeId(),
                raftLogTable.getLastLogIndex(), raftLogTable.getLastLogTerm()));
        // 设置一个选举动作的触发时间
        ElectionTimeoutEvent electionTimeoutEvent = new ElectionTimeoutEvent(MAX_CANDIDATE_HOLD_ON_SPAN_TIME,
                false, mote.getSimulator(), mote, constantVariable.currentTerm);
        mote.setTimeout(electionTimeoutEvent);
    }

    /**
     * Leader发送心跳包
     * 有许多逻辑需要进行交互完成，相关的处理逻辑参考以下两个方法：
     * <p>
     * receiveRPCHeartBeats
     * receiveRPCHeartBeatsResp
     */
    @Override
    public void TIMER_BEATS() {
        if (!leaderVariable.shouldBeatsCheck()) {
            return;
        }
        // 如果可以发送心跳包 自身也要不断刷新选举时长，总而避免触发再次选举动作
        // 当然这里有两种处理方式，另一种方式，也可以在 shouldElectCheck 对身份进行校验
        constantVariable.refreshElectionActionTime();
        // 获得自己当前最新的日志信息
        int lastLogIndex = raftLogTable.getLastLogIndex();
        for (int id = 1; id <= NODE_NUM; id++) {
            if (id == mote.getNodeId()) {
                // 不会发送给自己
                continue;
            }
            leaderVariable.leaderSendHeartBeats(id);
        }
        mote.print(raftLogTable.toString());
    }


    /**
     * 处理数据包
     * 数据包的内容，应当是一个RPC
     *
     * @param packet 数据包
     */
    @Override
    public void handlePacket(List<NetField> packet) {
        byte[] dataBytes = packet.get(1).value();
        String data = new String(dataBytes);
        //从数据包中取得第一个条数据 --- Raft数据包 标识
        int rpcType = RaftUtils.getFirstValFromString(data);
        switch (rpcType) {
            case RPC.RPC_ELECT -> receiveRPCElect(packet);
            case RPC.RPC_ELECT_RESP -> receiveRPCElectResp(packet);
            case RPC.RPC_HEARTBEATS -> receiveRPCHeartBeats(packet);
            case RPC.RPC_HEARTBEATS_RESP -> receiveRPCHeartBeatsResp(packet);
            default -> {
                //没有可以处理的
            }
        }
    }

    /**
     * <p>
     * Raft日志操作函数
     * 该函数应当通过外部进行触发，故在本仿真程序中，
     * 通过<strong>RaftOpCommand</strong>进行触发。
     * 命令通过 Command -> Mote -> Role 传递动作
     * <p>
     * RaftOpCommand触发逻辑中，必须要求该Mote必修是一个RaftMote。
     * 触发的是RaftMote的logOperate函数，进而触发了RaftRole的logOperate
     *
     * @param operationType 日志操作类型
     * @param key           操作键
     * @param value         操作值
     * @see RaftOpCommand
     * @see RaftNode
     * @see RaftRoleLogic
     */
    @Override
    public void logOperate(String operationType, String key, String value) {
        //检查自己是否是Leader
        if (role != ROLE_LEADER) {
            //如果不是Leader，没有权力进行日志操作
            return;
        }
        //调用 RaftLogTable 对该条日志进行录入
        raftLogTable.addLogInLast(operationType, key, value, constantVariable.currentTerm);
    }

    /**
     * 获取到日志表
     *
     * @return 日志表对象
     * @see RaftLogTable
     */
    @Override
    public RaftLogTable getLogTable() {
        return raftLogTable;
    }


    // 4个不同数据包的处理转发函数 //

    /**
     * 接受到心跳包的回复
     *
     * @param packet 数据包（心跳包回执）
     */
    private void receiveRPCHeartBeatsResp(List<NetField> packet) {
        String data = new String(packet.get(1).value());
        HeartBeatsRespRPC respRPC = new HeartBeatsRespRPC(data);
        int followerId = respRPC.getSenderId();
        if (respRPC.getIsMatched() == 1) {
            /*
            如果isMatched == 1，
            代表Leader与该Follower之间，已经达成了一致，
            此处涉及到对该Follower的相关更新，就必须得根据 Follower回传的matchIndex
             */
            leaderVariable.matchIndexes[followerId] = respRPC.getMatchIndex();
            leaderVariable.nextIndexes[followerId] = respRPC.getMatchIndex() + 1;
            leaderVariable.leaderReceiveRightHeartBeatsResp(respRPC);
        } else {
            /*
            如果isMatched == 0，
            代表Leader与该Follower之间，仍未达成一致
            此处，我们需要不断降低对该Follower的nextIndex
             */
            if (leaderVariable.nextIndexes[followerId] == 1) {
                //如果这个时候，它都仍出现了未达成一致
                //理论上这种情况不会出现
                new RaftRuntimeException("the nextIndex of " + followerId + " can't be smaller than 1").printStackTrace();
                return;
            }
            leaderVariable.nextIndexes[followerId]--;
            leaderVariable.matchIndexes[followerId] = 0;
            // 此时会立即补发一次请求，以尽快地同步
            leaderVariable.leaderSendHeartBeats(followerId);
        }
    }

    /**
     * 接受到心跳包 响应数据包
     *
     * @param packet 数据包（心跳包）
     */
    private void receiveRPCHeartBeats(List<NetField> packet) {
        String data = new String(packet.get(1).value());
        HeartBeatsRPC beatsRPC = new HeartBeatsRPC(data);
//        mote.print("beats : " + beatsRPC.toString());
        mote.print(raftLogTable.toString());
        //刷新一下动作时长，防止触发选举
        constantVariable.refreshElectionActionTime();
        if (beatsRPC.getPrevIndex() > raftLogTable.getLastLogIndex()) {
            /*
            如果传递过来的 prevIndex > lastLogIndex ：
            就说明Leader记录自己的nextIndex发生了不一致 应当回复 false
             */
            HeartBeatsRespRPC respRPC = new HeartBeatsRespRPC(RPC.RPC_HEARTBEATS_RESP,
                    constantVariable.currentTerm, 0, 0, mote.getNodeId());
            raftSender.uniCast(beatsRPC.getSenderId(), respRPC);
        } else {
            /*
            prevIndex <= lastLogIndex :
            说明自身记录着的一些日志可能是不匹配的，
            需要移除掉一部分日志：
                每个Leader它对每个用户的发送的 prevIndex 都是来自于 nextIndex 的
                而每个nextIndex最初都是和 Leader 的 lastLogIndex 靠齐的
                所以如果prevIndex 变小了， 肯定是之前的没有匹配导致的结果，
             */
            //第一步，我们需要核实 prevIndex 这条日志，及其 term，是否与我们自身存储的对应日志相同
            RaftLogItem prevLogItem = raftLogTable.getLogByIndex(beatsRPC.getPrevIndex());
            if (prevLogItem.getTerm() != beatsRPC.getPrevTerm()) {
                /*
                针对传入的 prevIndex ，本机对应的日志的prevTerm != RPC中的prevTerm
                说明我们与Leader的该条日志仍不匹配
                我们需要删除我们的这条日志
                 */
                raftLogTable.deleteAt(beatsRPC.getPrevIndex());
                // 仍需要给予false的回复
                HeartBeatsRespRPC respRPC = new HeartBeatsRespRPC(RPC.RPC_HEARTBEATS_RESP,
                        constantVariable.currentTerm, 0, 0, mote.getNodeId());
                raftSender.uniCast(beatsRPC.getSenderId(), respRPC);
            } else {
                /*
                反之，如果对于传入的 prevIndex ，本机对应的日志的prevTerm == RPC中的prevTerm
                说明我们与Leader的该条日志已经匹配了
                 */
                RaftLogItem logItem = beatsRPC.getLogItem();
                if (logItem != null) {
                    /*
                    这里添加日志的时候，一定要指明添加的index！！
                    不然，假设第二个相同的心跳包，也应该会触发该逻辑，
                    如果直接调用 raftLogTable.addLog(logItem) ，
                    它会将这个相同的心跳包的日志内容，直接再加到日志表的末尾去
                     */
                    raftLogTable.addLog(logItem, beatsRPC.getPrevIndex() + 1);
                }
                raftLogTable.tryToSetCommitIndex(beatsRPC.getCommitIndex());
                // 给予回复
                HeartBeatsRespRPC respRPC = new HeartBeatsRespRPC(RPC.RPC_HEARTBEATS_RESP,
                        constantVariable.currentTerm, 1, raftLogTable.getLastLogIndex(), mote.getNodeId());
                raftSender.uniCast(beatsRPC.getSenderId(), respRPC);
            }
        }

    }

    /**
     * 接受到了选举数据包的 响应数据包
     *
     * @param packet 数据包（选举包回执）
     */
    private void receiveRPCElectResp(List<NetField> packet) {
        String data = new String(packet.get(1).value());
        ElectionRespRPC rpc = new ElectionRespRPC(data);
        if (rpc.getVoteGranted() == 1) {
            /*
            如果获得了选票，我们会增加选票的计数器
            同时对计数器的结果进行核查，检查自身的票数是否过半，过半直接成为Leader
             */
            candidateVariable.gotVoteNum++;
            if (candidateVariable.hasGotEnoughVoteToBeLeader()) {
                //选票过半，成为Leader
                leaderVariable.TO_LEADER();
            }
        } else {
            /*
            如果没有获得选票：
            (1)· Follower 已经投给了其他人
            (2)· 自身的日志没有满足匹配要求
            (3)· 自身的term没有超过follower

            特别是(3)是一个涉及到自身角色发生变化的状况
            前两种，都不会影响到Leader的决策
            我们着重针对 term 字段，来排查是否是情况(3)
             */
            if (rpc.getTerm() > constantVariable.currentTerm) {
                /*
                需要注意的是，即便 candidate's term == 7 , follower's term == 6.
                在该 follower 给 candidate 的响应中， RESP_RPC's term == 7

                正好，比如A 和 B 都是 Candidate，term 都是 7
                C 先受到 A 的请求，C 给了 A 选票，并且告知了自己的term = 7。
                C 后受到 B 的请求，C 拒绝了 B ，没有给出选票，并且告知了自己的term = 7

                关键重点是B（A没有特别需要注意的）。B自身的term 刚好等于了 C的term，
                B显然不能因为C的term（A的term传递过来的）与自己相等，自己就变为Follower
                故只有当RESP_RPC's term 大于 currentTerm 的时候，才会变为Follower
                 */
                try {
                    constantVariable.TO_FOLLOWER(rpc.getTerm(), rpc);
                } catch (ParameterException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 接收到一个选举数据包
     * 会触发动作计时刷新
     *
     * @param packet 数据包（选举包）
     */
    private void receiveRPCElect(List<NetField> packet) {
        String data = new String(packet.get(1).value());
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
                        constantVariable.currentTerm, 1, mote.getNodeId());
                raftSender.uniCast(rpc.getSenderId(), respRpc);
                // 有效操作后 重置一下动作时间
                constantVariable.refreshElectionActionTime();
            } catch (ParameterException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            //针对该选票进行拒绝
            ElectionRespRPC respRPC = new ElectionRespRPC(RPC.RPC_ELECT_RESP,
                    constantVariable.currentTerm, 0, mote.getNodeId());
            raftSender.uniCast(rpc.getSenderId(), respRPC);
        }
    }

    // 节点内部接口 //

    /**
     * 检查该选举RPC是否满足要求
     *
     * @param rpc 选举RPC
     * @return true ： 该 Candidate可以从我这里获取选票
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

    // 内部类定义部分 //

    /**
     * Leader 会使用到的变量
     * nextIndexes、matchIndexes、voteGranted
     * <p>
     * 需要对数组与对象的<strong>下标关系</strong>进行重点说明：
     * <pre>
     *     int moteId = 4;
     *     mote_4's info = nextIndexes[moteId]
     * </pre>
     * 因为 nextIndexes、matchIndexes、voteGranted 的实际数组大小均为 n + 1
     * <p>
     * 所以我们这边的下标，就设计成了 <strong>Id == index</strong>
     */
    protected class LeaderVariable {
        /**
         * Raft节点个数
         */
        int n;
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

        List<CommitCheckList> matchRecords;

        /**
         * @param n Raft节点个数
         */
        public LeaderVariable(int n) {
            this.n = n;
            nextIndexes = new int[n + 1];
            matchIndexes = new int[n + 1];
            voteGranted = new int[n + 1];
            matchRecords = new ArrayList<>();
            clear();
        }

        /**
         * 清空对象内变量
         */
        private void clear() {
            int lastLogIndex = raftLogTable.getLastLogIndex();
            for (int i = 1; i <= n; i++) {
                nextIndexes[i] = lastLogIndex + 1;
                matchIndexes[i] = 0;
                voteGranted[i] = 0;
            }
        }

        /**
         * 成为 Leader
         */
        public void TO_LEADER() {
            role = ROLE_LEADER;
            //成为领导，对参选时的变量进行清空
            candidateVariable.clearVoteAndActionTime();
            constantVariable.clearVotedAndLeader();
            leaderVariable.clear();
            mote.print("I become the leader");
        }

        /**
         * 检查是否有资格发送HeartBeats包
         * <p>
         * called by TIMER_BEATS()
         *
         * @return
         */
        public boolean shouldBeatsCheck() {
            return role == ROLE_LEADER;
        }

        /**
         * Leader 接受到来自Follower的 正确的 心跳包响应
         *
         * @param respRPC 心跳包响应
         */
        private void leaderReceiveRightHeartBeatsResp(HeartBeatsRespRPC respRPC) {
            int matchIndex = respRPC.getMatchIndex();
            if (matchIndex <= raftLogTable.getCommitIndex()) {
                //如果返回的matchIndex已经小于等于了我们的commitIndex
                //显然本次不在需要对其做任何的操作
                clearUnNeededCheckList(raftLogTable.getCommitIndex());
                return;
            }
            CommitCheckList suitableCheckList = getSuitableCheckList(matchIndex);
            boolean hasAchievedConsensus = suitableCheckList.addVisitedAndGetResult(respRPC.getSenderId());
            if (hasAchievedConsensus) {
                raftLogTable.tryToSetCommitIndex(suitableCheckList.getCommitIndex());
            }
            //尝试清理掉不合适的列表
            clearUnNeededCheckList(raftLogTable.getCommitIndex());
        }

        private CommitCheckList getSuitableCheckList(int matchIndex) {
            //如果返回的matchIndex比我们的commitIndex更大
            for (CommitCheckList checkList : matchRecords) {
                int listIndex = checkList.getCommitIndex();
                if (listIndex == matchIndex) {
                    //找到了List
                    return checkList;
                }
                if (listIndex > matchIndex) {
                    break;
                }
            }
            CommitCheckList list = new CommitCheckList(matchIndex);
            matchRecords.add(list);
            Collections.sort(matchRecords);
            return list;
        }

        /**
         * 我们会将commitIndex之前的 checkList 全部清除
         * 调用处应当在接受到“心跳包响应”的时候
         *
         * @param commitIndex RaftLogTable中存储的commitIndex
         */
        private void clearUnNeededCheckList(int commitIndex) {
            for (int i = 0; i < matchRecords.size(); i++) {
                CommitCheckList commitCheckList = matchRecords.get(i);
                int listIndex = commitCheckList.getCommitIndex();
                if (listIndex <= commitIndex) {
                    matchRecords.remove(commitCheckList);
                    i--;
                } else {
                    break;
                }
            }
        }

        /**
         * 领导发送心跳包给某个节点
         *
         * @param moteId 节点Id
         */
        private void leaderSendHeartBeats(int moteId) {
            // 获取我们判定运算的三个核心参数
            int lastLogIndex = raftLogTable.getLastLogIndex();
            int nextIndex = nextIndexes[moteId];
            int matchIndex = matchIndexes[moteId];
            int commitIndex = Math.min(raftLogTable.getCommitIndex(), matchIndex);
            // 计算 prevIndex 与 prevTerm 这些参数
            int prevIndex = nextIndex - 1;
            RaftLogItem transferLogItem = raftLogTable.getLogByIndex(prevIndex);
            int prevTerm = transferLogItem.getTerm();
            /*
            尝试传输该条日志对象(raftLog[nextIndex])之前，
            我们必须确保nextIndex之前的日志是与我们匹配的
             */
            if (matchIndex != nextIndex - 1) {
                /*
                当不匹配时 我们需要去确认该 matchIndex 是否满足
                此时不传输日志对象
                等待函数receiveRPCHeartBeats的返回值
                我们在函数receiveRPCHeartBeatsResp中，对返回值进行处理，
                进而影响 nextIndexes 和 matchIndexes 与
                */
                HeartBeatsRPC rpc = new HeartBeatsRPC(RPC.RPC_HEARTBEATS,
                        constantVariable.currentTerm, mote.getNodeId(),
                        prevIndex, prevTerm, commitIndex, null);
                raftSender.uniCast(moteId, rpc);
            } else {
                /*
                如果我们对该Follower，满足以下关系：
                matchIndex == nextIndex - 1
                那么这说明我们已经确定了该Follower的日志进度，但并不是说和我们日志进度相同。
                例如记录的该结点的 nextIndex = 5, matchIndex = 4,
                而Leader自身的 lastLogIndex = 7
                这说明 Leader 与 该Follower [1,4] 是一样的。
                但是显然 Follower 仍不具有与 Leader 相同的日志，也就是[5,7] 这部分日志。
                */
                if (nextIndex <= lastLogIndex) {
                    /*
                    匹配后，我们将对自身lastLogIndex进行判定
                    进而得出是否需要发送LogItem
                     */
                    RaftLogItem item = raftLogTable.getLogByIndex(nextIndex);
                    HeartBeatsRPC rpc = new HeartBeatsRPC(RPC.RPC_HEARTBEATS,
                            constantVariable.currentTerm, mote.getNodeId(),
                            prevIndex, prevTerm, commitIndex, item);
                    raftSender.uniCast(moteId, rpc);
                } else {
                    // 说明目标与我们的日志已经同步了，无需再传输新的数据
                    HeartBeatsRPC rpc = new HeartBeatsRPC(RPC.RPC_HEARTBEATS,
                            constantVariable.currentTerm, mote.getNodeId(),
                            prevIndex, prevTerm, commitIndex, null);
                    raftSender.uniCast(moteId, rpc);
                }
            }
        }

        /**
         * 提交确认列表
         * <p>
         * 该类是Leader专用的类，专门用于确认commitIndex的变化
         * <p>
         * 该类其实是一个{key, value}逻辑的对象。
         * commitIndex作为key、visited作为value。
         * <p>
         * 每一个commitIndex，都对应了一个记录数组
         * 如果一个记录数组记录过半，就视为该commitIndex已经达成共识。
         */
        class CommitCheckList implements Comparable<CommitCheckList> {
            int commitIndex;
            int visitCount;
            boolean[] visited;

            public CommitCheckList(int commitIndex) {
                this.commitIndex = commitIndex;
                this.visited = new boolean[n + 1];
                this.visitCount = 1;
                this.visited[mote.getNodeId()] = true;
            }

            /**
             * 取得当前List的 key值，也就是所属的commitIndex值
             *
             * @return 待commit的commitIndex
             */
            public int getCommitIndex() {
                return commitIndex;
            }

            /**
             * 取得当前commitIndex值对应的记录数组
             *
             * @return 所属commitIndex值对应的记录数组
             */
            public boolean[] getVisited() {
                return visited;
            }

            /**
             * 往记录数组中新增一条记录
             *
             * @param senderId 记录的用户id，取值应为(1 ~ n)
             */
            public boolean addVisitedAndGetResult(int senderId) {
                if (!visited[senderId]) {
                    visited[senderId] = true;
                    visitCount++;
                    return visitCount > n / 2;
                }
                return false;
            }

            @Override
            public int compareTo(CommitCheckList o) {
                return this.commitIndex - o.commitIndex;
            }
        }
    }

    /**
     * hasGotVote 自己作为Candidate，取得的选票数量
     * candidateActionLimitTime 候选动作终止时间
     */
    protected class CandidateVariable {
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
                case ROLE_CANDIDATE -> {
                    this.clearVoteAndActionTime();
                    role = ROLE_FOLLOWER;
                    mote.print("选票不够，选举失败");
                }
                case ROLE_LEADER -> mote.print("已经选举成功");
                case ROLE_FOLLOWER -> {
                    this.clearVoteAndActionTime();
                    mote.print("因为其他Candidate或者Leader，选举失败");
                }
                default -> {
                }
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
    protected class ConstantVariable {
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
            refreshElectionActionTime();
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
         * 刷新Election的触发时间
         * 应该调用的场景：
         * <p>
         * (1)·每当你接收到 RPC_HEARTBEATS 和 RPC_ELECT 的时候，
         * <p>
         * (2)·如果仅有条件(1)，Leader自身是不会接受到这两个数据包的。
         * 那么Leader也会因为时长的到达而触发Elect逻辑。
         * 所以我们可以让TIMER_BEATS() 方法也调用该方法
         * <p>
         * 触发原理如下：
         * 使用RaftUtils.floatValue(MAX_ELECT_SPAN_TIME ,MAX_ELECT_SPAN_TIME_FLOAT)
         * 计算出增量时间，
         * <p>
         * 让 mote.simulator.getTime() 得到的当前时间，去加上增量时间。
         * 进而得到 constantVariable 中的新的 electActionLimitTime。
         *
         * @see RaftUtils
         * @see com.sicnu.netsimu.core.NetSimulator
         */
        protected void refreshElectionActionTime() {
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
        public void TO_FOLLOWER(int term, @NotNull SenderRPC rpc) throws ParameterException, ClassNotFoundException {
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
            int senderId = rpc.getSenderId();
            if (rpc instanceof ElectionRPC) {
                //如果是一个选举请求 自身会把votedFor进行设置
                constantVariable.votedFor = senderId;
            } else if (rpc instanceof HeartBeatsRPC) {
                constantVariable.currentLeaderId = senderId;
            } else if (rpc instanceof ElectionRespRPC) {
                // 这种情况是，我想要成为Leader，但是对方的term比我更高，所以给予了我拒绝
                // 自身必须以对方为Leader
                constantVariable.currentLeaderId = senderId;
            } else {
                throw new ClassNotFoundException("没有找到合适的类");
            }
            mote.print("I become the follower ( term is " + term + " , leader is: " + rpc.getSenderId() + " )");
        }

        /**
         * 是否有权力执行 选举检查
         *
         * @return true == 可以进行选举
         */
        public boolean shouldElectCheck() {
            long time = mote.getSimulator().getTime();
            //如果超时了 此时才可以进行选举
            return time > electActionLimitTime;
        }

        /**
         * 每个节点成为Follower的时候，不一定有Leader
         * 因为节点会因为其他节点的RPC-ELECT，而变为Follower
         * <p>
         * 只要节点接受到一次心跳包后，那么它会去更改自己的currentLeaderId
         * 此时就可以说明自己已经有Leader了。
         * <p>
         * 这个方法具体在哪里可能会被调用，目前我也不太清楚
         *
         * @return true == 代表自己有Leader
         */
        public boolean hasGotLeader() {
            return currentLeaderId != 0;
        }

    }

    /**
     * 选举事件，不再是匿名类，
     * <p>
     * 在该事件结束后，我们会对Leader的状态进行判定，判定是否有流局现象。
     */
    public class ElectionTimeoutEvent extends TimeoutEvent {
        /**
         * 当前正在竞选的term，
         * 该变量主要是交给RaftSummarizer在统计选举成功和失败信息的时候使用
         *
         * @see com.sicnu.raft.ui.RaftCalculateInPreEventInterceptor
         * @see com.sicnu.raft.ui.RaftSummarizer
         */
        int term;

        /**
         * @param spanTime  延时间隔
         * @param isLoop    是否循环
         * @param simulator 模拟器引用
         * @param selfNode  调用setTimeout的节点引用
         */
        public ElectionTimeoutEvent(long spanTime, boolean isLoop, NetSimulator simulator, Node selfNode, int term) {
            super(spanTime, isLoop, simulator, selfNode);
            this.term = term;
        }

        /**
         * 每个事件都有自己的专属方法。
         */
        @Override
        public void work() {
            // 选举超时结束动作
            candidateVariable.candidateElectionEnding();
        }

        public int getTerm() {
            return term;
        }
    }
}
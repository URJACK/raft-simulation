package com.sicnu.raft.mote;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.net.BasicNetStack;
import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.statis.EnergyCost;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.raft.command.RaftOpCommand;
import com.sicnu.netsimu.exception.ParseException;
import com.sicnu.raft.role.BasicRaftRole;
import com.sicnu.raft.role.RaftRole;
import com.sicnu.raft.RaftUtils;
import com.sicnu.raft.role.log.RaftLogTable;

import java.util.List;

/**
 * Raft节点 的protected方法名
 * containPort
 * containAddress
 * listenPort
 * listenIp
 * print
 * setTimeout
 * call
 */
public class RaftMote extends Mote {
    public static final String IP_PREFIX = "192.168.0.";
    public static final String MAC_PREFIX = "EE:EE:EE:EE:EE:";
    public static final int RAFT_PORT = 3000;
    private int NODE_NUM = 0;
    RaftRole raftRole;
    /**
     * 触发选举操作的检查时间
     * 触发该时间后，未必就会进行选举，它会调用role去检查是否可以进行选举。
     */
    private static final int TRIGGER_TIME = 100;

    /**
     * 其他节点的构造函数只能在这基础上实现
     * 同时构造函数传入的参数不可以改变
     *
     * @param simulator 模拟器引用
     * @param moteId    节点Id
     * @param x         节点x坐标
     * @param y         节点y坐标
     */
    public RaftMote(NetSimulator simulator, int moteId, float x, float y, String... args) {
        super(simulator, moteId, x, y, RaftMote.class);
        //监听ip地址 合成每个节点的专属Ip地址
        String selfMacAddress = MoteCalculate.convertMACAddressWithMoteId(MAC_PREFIX, moteId);
        //Raft节点记录下的NODE_NUM数
        NODE_NUM = Integer.parseInt(args[0]);
        raftRole = new BasicRaftRole(this, NODE_NUM);
        equipNetStack(selfMacAddress);
    }

    /**
     * 初始化函数
     */
    @Override
    @EnergyCost(10f)
    public void init() {
        //每隔 ELECT_SPAN_TIME
        setTimeout(new TimeoutEvent(TRIGGER_TIME, true, simulator, this) {
            @Override
            public void work() {
                // 尝试进行选举动作
                raftRole.TIMER_ELECT();
                // 尝试发送心跳包
                raftRole.TIMER_BEATS();
            }
        });
    }

    /**
     * 由构造函数进行调用
     * <pre>
     * public Mote(){
     *     //....
     *     equipNetStack()
     * }
     * </pre>
     * 配备网络栈
     *
     * @param args 初始化网络栈参数
     */
    @Override
    public void equipNetStack(Object... args) {
        String macAddress;
        if (args[0] instanceof String) {
            macAddress = (String) args[0];
        } else {
            new ParseException("NetStack init error, no suitable macAddress").printStackTrace();
            return;
        }
        this.netStack = new BasicNetStack(macAddress);
    }

    /**
     * @param packet 接受到的数据包
     */
    @Override
    @EnergyCost(30f)
    public void netReceive(String packet) {
        List<NetField> netFields = netStack.parse(packet);
        if (netFields != null) {
            raftRole.handlePacket(netFields);
        }
    }

    /**
     * Raft算法的日志的相关操作，被RaftOpCommand触发，
     * 调用自身raftRole的引用的同名函数logOperate进行工作。
     * <p>
     * 命令通过 Command -> Mote -> Role 的传递链传递动作
     *
     * @param operationType 操作类型
     * @param key           操作键
     * @param value         操作值
     * @see RaftOpCommand
     * @see RaftMote
     * @see RaftRole
     */
    public void logOperate(String operationType, String key, String value) {
        raftRole.logOperate(operationType, key, value);
    }

    /**
     * 返回日志表的引用，是从RaftRole -> RaftMote -> ... 这样返回上来的
     *
     * @return 日志表引用
     * @see RaftLogTable
     * @see RaftRole
     */
    public RaftLogTable getLogTable() {
        return raftRole.getLogTable();
    }
}

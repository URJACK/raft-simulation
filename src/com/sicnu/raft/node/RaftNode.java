package com.sicnu.raft.node;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.net.BasicNetStack;
import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.statis.EnergyCost;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.raft.command.RaftOpCommand;
import com.sicnu.netsimu.exception.ParseException;
import com.sicnu.raft.role.RaftRoleLogic;
import com.sicnu.raft.log.RaftLogTable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 构建节点的Command如下：
 * <pre>
 * 1000, NODE_ADD, 3, 250, 100, com.sicnu.raft.mote.RaftMote, 3, com.sicnu.raft.role.BasicRaftRoleLogic
 * </pre>
 */
public class RaftNode extends Node {
    //    public static final String IP_PREFIX = "192.168.0.";
//    public static final String MAC_PREFIX = "EE:EE:EE:EE:EE:";
    public static final byte[] MAC_PREFIX = {(byte) 0xEE, (byte) 0xEE,
            (byte) 0xEE, (byte) 0xEE, (byte) 0xEE};
    public static final int RAFT_PORT = 3000;
    RaftRoleLogic raftRoleLogic;
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
    public RaftNode(NetSimulator simulator, int moteId, float x, float y, Class moteClass, String... args) {
        super(simulator, moteId, x, y, moteClass);
        try {
            //Raft节点记录下的NODE_NUM数
            int NODE_NUM = Integer.parseInt(args[0]);
            //监听ip地址 合成每个节点的专属Ip地址
            byte[] selfMacAddress = MoteCalculate.convertMACAddressWithMoteId(MAC_PREFIX, moteId);
            String rolePath = args[1];
            Class<?> roleClazz = Class.forName(rolePath);
            Constructor<?> constructor = roleClazz.getDeclaredConstructor(RaftNode.class, int.class);
            raftRoleLogic = (RaftRoleLogic) constructor.newInstance(this, NODE_NUM);
            equipNetStack((Object) selfMacAddress);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | ParseException e) {
            e.printStackTrace();
        }
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
                raftRoleLogic.TIMER_ELECT();
                // 尝试发送心跳包
                raftRoleLogic.TIMER_BEATS();
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
        byte[] macAddress;
        if (args[0] instanceof byte[]) {
            macAddress = (byte[]) args[0];
        } else {
            new ParseException("NetStack init error, no suitable macAddress").printStackTrace();
            return;
        }
        this.netStack = new BasicNetStack(macAddress);
    }

    /**
     * 以太网发送结果回调函数
     *
     * @param data   数据包
     * @param result 发送结果
     */
    @Override
    public void netSendResult(byte[] data, boolean result) {
        System.out.println("sending " + data + " " + result);
    }

    /**
     * @param packet 接受到的数据包
     */
    @Override
    @EnergyCost(30f)
    public void netReceive(byte[] packet) {
        List<NetField> netFields = netStack.parse(packet);
        if (netFields != null) {
            raftRoleLogic.handlePacket(netFields);
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
     * @see RaftNode
     * @see RaftRoleLogic
     */
    public void logOperate(String operationType, String key, String value) {
        raftRoleLogic.logOperate(operationType, key, value);
    }

    /**
     * 返回日志表的引用，是从RaftRole -> RaftMote -> ... 这样返回上来的
     *
     * @return 日志表引用
     * @see RaftLogTable
     * @see RaftRoleLogic
     */
    public RaftLogTable getLogTable() {
        return raftRoleLogic.getLogTable();
    }

    /**
     * 获取到当前节点的角色信息。
     * <p>
     * 因为角色信息没有存储在RaftMote这个类中，而是存储在RaftRole中，
     * 所以我们这里要通过RaftRole对角色信息进行返还
     *
     * @see RaftRoleLogic
     */
    public int getRole() {
        return raftRoleLogic.getRole();
    }

    /**
     * RaftMote发送心跳包
     * 发送心跳包的函数是RaftRole进行编写的。
     *
     * @see RaftRoleLogic
     */
    public void sendHeartBeats() {
        //节点角色发送心跳包
        raftRoleLogic.TIMER_BEATS();
    }
}

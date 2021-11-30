package com.sicnu.netsimu.raft.mote;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.statis.EnergyCost;

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
    private static final String IP_PREFIX = "192.168.0.";
    private static final int RAFT_PORT = 3000;

    /**
     * 其他节点的构造函数只能在这基础上实现
     * 同时构造函数传入的参数不可以改变
     *
     * @param simulator 模拟器引用
     * @param moteId    节点Id
     * @param x         节点x坐标
     * @param y         节点y坐标
     */
    public RaftMote(NetSimulator simulator, int moteId, float x, float y, Object... args) {
        super(simulator, moteId, x, y, RaftMote.class);
        //监听ip地址
        String ipStr = IP_PREFIX + moteId;
        //监听该ip地址
//        listenIp(ipStr);
        call("listenIp", ipStr);
        //监听该端口
        call("listenPort", RAFT_PORT);
    }

    @Override
    @EnergyCost(10f)
    public void init() {

    }

    @Override
    @EnergyCost(30f)
    public void netReceive(TransmissionPacket packet) {

    }
}

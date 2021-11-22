package com.sicnu.raftsimu.core.mote;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.event.TransmissionEvent;
import com.sicnu.raftsimu.core.event.trans.TransmissionManager;
import com.sicnu.raftsimu.core.event.trans.TransmissionPacket;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 节点对象 是我们仿真程序中的一个基础类
 * 每个节点对象之间的各类交互，就是我们的仿真的基础
 */
@Data
public abstract class Mote {
    protected RaftSimulator simulator;
    protected int moteId;
    protected float x;
    protected float y;
    protected List<String> registerIpAddressList;
    protected List<Integer> registerPortList;

    public Mote() {
    }

    /**
     * @param simulator 模拟器对象引用
     * @param moteId    节点的id
     * @param x         节点的x坐标
     * @param y         节点的y坐标
     */
    public Mote(RaftSimulator simulator, int moteId, float x, float y) {
        this.simulator = simulator;
        this.moteId = moteId;
        this.x = x;
        this.y = y;
        registerIpAddressList = new LinkedList<>();
        registerPortList = new LinkedList<>();
    }

    /**
     * 网络接收函数
     *
     * @param packet 接受到的数据包
     */
    public abstract void netReceive(TransmissionPacket packet);

    /**
     * 网络发送函数
     *
     * @param packet 发送的数据包
     */
    public boolean netSend(TransmissionPacket packet) {
        //检查自身当前是否可以发出该数据包
        if (!containAddress(packet.getSrcIp()) || !containPort(packet.getSrcPort())) {
            //如果自身并不包含这个ip地址 或者 存在不包含这个端口号的情况
            //那么本次发送数据包就会失败
            return false;
        }
        TransmissionManager transmissionManager = simulator.getTransmissionManager();
        List<TransmissionManager.Neighbor> neighbors = transmissionManager.getNeighbors(this);
        //从传输管理器中 查询该节点的邻居节点
        for (TransmissionManager.Neighbor neighbor : neighbors) {
            //获取到与neighbor的距离
            float distance = neighbor.getDistance();
            //获取到neighbor指向的mote本身
            Mote mote = neighbor.getMote();
            //无论该节点的ip和端口信息是否满足 数据包的目的地要求 我们都将其进行传输
            simulator.getEventManager().pushEvent(
                    new TransmissionEvent(transmissionManager.getTriggerTime(distance) + simulator.getNowTime(),
                            mote, packet)
            );
        }
        return true;
    }

    /**
     * 查看当前Mote，是否注册监听了这个端口
     *
     * @param port 端口
     * @return
     */
    protected boolean containPort(int port) {
        for (Integer integer : registerPortList) {
            if (integer == port) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查看当前Mote，是否包含有这个Ip地址
     *
     * @param ip ip地址
     * @return
     */
    protected boolean containAddress(String ip) {
        for (String s : registerIpAddressList) {
            if (s.equals(ip)) {
                return true;
            }
        }
        return false;
    }
}

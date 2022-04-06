package com.test.testpk;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.net.mac.IEEE_802_11_MACLayer;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.net.BasicNetStack;
import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.net.NetStack;
import com.sicnu.netsimu.core.statis.EnergyCost;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.netsimu.exception.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 基础节点
 */
public class TestNodeA extends Node {

    //    public static final byte[] MAC_PREFIX = "EE:EE:EE:EE:EE:";
    public static final byte[] MAC_PREFIX = {(byte) 0xEE, (byte) 0xEE, (byte) 0xEE,
            (byte) 0xEE, (byte) 0xEE};

    private int sendCount;

    /**
     * @param simulator 模拟器对象引用
     * @param moteId    节点的id
     * @param x         节点的x坐标
     * @param y         节点的y坐标
     */
    public TestNodeA(NetSimulator simulator, int moteId, float x, float y, Class moteClass, String... args) {
        super(simulator, moteId, x, y, moteClass);
        this.sendCount = 0;
        try {
            byte[] selfMacAddress = MoteCalculate.convertMACAddressWithMoteId(MAC_PREFIX, moteId);
            equipNetStack((Object) selfMacAddress);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    @EnergyCost(4f)
    public void init() {
        TimeoutEvent event = new TimeoutEvent(500, true, simulator, this) {
            @Override
            public void work() {
//                call("print", "我是节点");
                // 间歇性 发送数据包
                BasicNetStack stack = getNetStack();
//                byte[] dstMac = BasicMACLayer.BROAD_CAST;
                byte[] dstMac = new byte[0];
                try {
                    if (getNodeId() == 1) {
                        return;
                    }
                    byte[] packet;
                    dstMac = MoteCalculate.convertMACAddressWithMoteId(MAC_PREFIX, 1);
                    packet = stack.generateMacSendingPacket(("I'm " + moteId + " seq:" + sendCount++ + " time:" + simulator.getTime()).getBytes(), dstMac);
                    netSend(packet);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        setTimeout(event);
    }

    @Override
    @EnergyCost(28f)
    public void netReceive(byte[] packet) {
        ArrayList<NetField> netFields = netStack.parse(packet);
        if (netFields == null) {
            //不满足
            return;
        }
        call("print", new String(netFields.get(1).value()));
//        call("print", new String(netFields.get(1).value()) + " " + simulator.getTime());
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
     * @param args 传入参数
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
        this.netStack = new BasicNetStack(this, macAddress);
    }

    /**
     * 以太网发送结果回调函数
     *
     * @param data   数据包
     * @param result 发送结果
     */
    @Override
    public void netSendResult(byte[] data, boolean result) {
        if (!result) {
            System.out.println("node:" + getNodeId() + " sending " + new String(data) + " failed");
        }
    }
}

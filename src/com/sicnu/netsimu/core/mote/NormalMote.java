package com.sicnu.netsimu.core.mote;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.net.BasicNetStack;
import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.statis.EnergyCost;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.netsimu.exception.ParseException;

import java.util.ArrayList;

/**
 * 基础节点
 */
public class NormalMote extends Mote {

    public static final String MAC_PREFIX = "EE:EE:EE:EE:EE:";

    /**
     * @param simulator 模拟器对象引用
     * @param moteId    节点的id
     * @param x         节点的x坐标
     * @param y         节点的y坐标
     */
    public NormalMote(NetSimulator simulator, int moteId, float x, float y, Class moteClass, String... args) {
        super(simulator, moteId, x, y, moteClass);
        String selfMacAddress = MoteCalculate.convertMACAddressWithMoteId(MAC_PREFIX, moteId);
        equipNetStack(selfMacAddress);
    }

    @Override
    @EnergyCost(4f)
    public void init() {
        TimeoutEvent event = new TimeoutEvent(500, true, simulator, this) {
            @Override
            public void work() {
                call("print", "我是节点");
            }
        };
        setTimeout(event);
    }

    @Override
    @EnergyCost(28f)
    public void netReceive(String packet) {
        ArrayList<NetField> netFields = netStack.parse(packet);
        if (netFields == null) {
            //不满足
            return;
        }
        call("print", netFields.get(1).value());
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
        String macAddress;
        if (args[0] instanceof String) {
            macAddress = (String) args[0];
        } else {
            new ParseException("NetStack init error, no suitable macAddress").printStackTrace();
            return;
        }
        this.netStack = new BasicNetStack(macAddress);
    }

}

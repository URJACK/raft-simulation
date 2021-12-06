package com.sicnu.netsimu.core.mote;

import com.sicnu.netsimu.core.net.BasicNetStack;
import com.sicnu.netsimu.core.net.NetStack;
import com.sicnu.netsimu.core.statis.EnergyCost;
import com.sicnu.netsimu.core.event.TimeoutEvent;
import com.sicnu.netsimu.core.statis.EnergyStatistician;
import com.sicnu.netsimu.core.statis.Statistician;
import com.sicnu.netsimu.core.utils.NetSimulationRandom;
import com.sicnu.netsimu.ui.InfoOutputManager;
import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TransmissionEvent;
import com.sicnu.netsimu.core.net.TransmissionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 节点对象 是我们仿真程序中的一个基础类
 * 每个节点对象之间的各类交互，就是我们的仿真的基础
 */
public abstract class Mote {
    protected NetSimulator simulator;
    protected int moteId;
    protected float x;
    protected float y;
    /**
     * 网络栈
     */
    protected NetStack netStack;
    /**
     * 获得MoteClass对象，通过反射方式执行函数，进而计算能耗
     */
    protected Class moteClass;
    /**
     * 统计者
     */
    protected Statistician<Float> energyStatistician;

    /**
     * 其他节点的构造函数只能在这基础上实现
     * 同时构造函数传入的参数不可以改变
     *
     * @param simulator 模拟器对象引用
     * @param moteId    节点的id
     * @param x         节点的x坐标
     * @param y         节点的y坐标
     * @param args      潜在的额外参数
     */
    public Mote(NetSimulator simulator, int moteId, float x, float y, Class moteClass, String... args) {
        this.simulator = simulator;
        this.moteId = moteId;
        this.x = x;
        this.y = y;
        energyStatistician = new EnergyStatistician(this);
        this.moteClass = moteClass;
    }

    /**
     * 节点创建后，一定会执行的函数
     */
    public abstract void init();

    /**
     * 网络接收函数
     *
     * @param packet 接受到的数据包
     */
    public abstract void netReceive(String packet);

    /**
     * 由构造函数进行调用
     * <pre>
     * public Mote(){
     *     //....
     *     equipNetStack()
     * }
     * </pre>
     * 配备网络栈
     */
    public abstract void equipNetStack(Object... args);


    /**
     * 网络发送函数
     *
     * @param packet 发送的数据包
     */
    @EnergyCost(30f)
    public boolean netSend(String packet) {
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
                    new TransmissionEvent(transmissionManager.calcTransmissionTime(distance) + simulator.getTime(),
                            mote, packet)
            );
        }
        return true;
    }


    /**
     * 打印信息到控制台，对应当前的Simulator的时间
     * [这里我们需要将要打印的信息缓存到ConsoleManager中]
     *
     * @param info 要打印的信息
     */
    @EnergyCost(5.2f)
    public final void print(String info) {
        InfoOutputManager infoOutputManager = simulator.getInfoOutputManager();
        infoOutputManager.pushInfo(simulator.getTime(), moteId, info);
    }

    /**
     * 设置一个延时事件
     *
     * @param event 事件本身
     */
    public final void setTimeout(TimeoutEvent event) {
        //添加一个新的事件
        simulator.getEventManager().pushEvent(event);
    }

    /**
     * 通过call调用函数
     * 依赖于 energyStatistician 来计算能耗
     *
     * @param methodName 方法名称
     * @param args       方法参数列表
     * @return 函数返回值对象
     */
    public Object call(String methodName, Object... args) {
        Method method = null;
        try {
            //获取每个参数的类型
            Class[] paramArgs = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramArgs[i] = args[i].getClass();
            }
            method = moteClass.getMethod(methodName, paramArgs);
            //执行该函数 得到函数返回值
            Object result = method.invoke(this, args);
            //获取到该函数的能耗对象
            EnergyCost annotation = method.getAnnotation(EnergyCost.class);
            //计算出该函数的能耗
            float energyCost;
            if (annotation == null) {
                energyCost = 0;
            } else {
                energyCost = annotation.value();
                float beta = annotation.beta();
                energyCost = (float) (energyCost * (1 - NetSimulationRandom.nextFloat() * beta));
            }
//            System.out.println("DEBUG cost " + energyCost);
            //将能耗统计进入统计家中
            energyStatistician.addValue(methodName, energyCost);
            //将函数执行的返回值返回出去
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    // getters  //

    public NetSimulator getSimulator() {
        return simulator;
    }

    public int getMoteId() {
        return moteId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Statistician<Float> getEnergyStatistician() {
        return energyStatistician;
    }

    // setters //

    public void setSimulator(NetSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * 取得 网络栈
     *
     * @return 返回网络栈对象引用
     */
    public NetStack getNetStack() {
        return netStack;
    }
}

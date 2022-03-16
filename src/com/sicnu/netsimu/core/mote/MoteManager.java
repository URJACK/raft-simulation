package com.sicnu.netsimu.core.mote;

import com.sicnu.netsimu.core.NetSimulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 节点管理器，下属于Simulator
 * 存储了节点的相关信息
 */
public class MoteManager {
    ArrayList<Mote> motes;
    HashMap<Integer, Mote> moteRecorder;
    NetSimulator simulator;

    /**
     * @param simulator 仿真器对象引用
     */
    public MoteManager(NetSimulator simulator) {
        motes = new ArrayList<>();
        moteRecorder = new HashMap<>();
        this.simulator = simulator;
    }

    /**
     * 节点是否已经存在
     *
     * @param nodeId 节点id
     * @return
     */
    public boolean containMote(int nodeId) {
        return moteRecorder.containsKey(nodeId);
    }

    /**
     * 通过反射获取节点类型，
     * 这里对节点的构造函数有参数要求
     * 必须形如 (simulator,nodeId,x,y) 的构造函数才行
     * <p>
     * 添加节点，节点的id不可以之前存在
     *
     * @param nodeId        节点的id
     * @param x             节点的x坐标
     * @param y             节点的y坐标
     * @param nodeClassPath 节点类型的反射地址
     * @param args          额外参数列表
     * @return 成功添加的新节点（失败则为null）
     */
    public Mote addMote(int nodeId, float x, float y, String nodeClassPath, String... args) {
        if (moteRecorder.containsKey(nodeId)) {
            //如果已经存在了这个节点
            return null;
        }
        try {
            Class nodeClass = Class.forName(nodeClassPath);
            Constructor[] declaredConstructors = nodeClass.getDeclaredConstructors();
            for (Constructor constructor : declaredConstructors) {
//                System.out.println(constructor.getName());
                Mote mote = (Mote) constructor.newInstance(simulator, nodeId, x, y, nodeClass, args);
//                NormalMote mote = new NormalMote(simulator, nodeId, x, y);
                motes.add(mote);
                moteRecorder.put(nodeId, mote);
                return mote;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取节点
     *
     * @param nodeId 节点的id
     * @return 通过节点id获得的节点
     */
    public Mote getMote(int nodeId) {
        return moteRecorder.get(nodeId);
    }

    /**
     * 将所有节点都进行获取
     *
     * @return 所有节点
     */
    public ArrayList<Mote> getAllMotes() {
        return motes;
    }

    /**
     * 删除节点
     *
     * @param nodeId 被删除的节点id
     */
    public void deleteMote(int nodeId) {

    }
}

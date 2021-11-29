package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;

/**
 * 总结者 用自己的方法来对整个数据进行总结统计
 */
public abstract class Summarizer {
    NetSimulator simulator;

    /**
     * @param simulator 网络模拟器对象引用
     */
    public Summarizer(NetSimulator simulator) {
        this.simulator = simulator;
    }

    public abstract void summarize(String param);
}

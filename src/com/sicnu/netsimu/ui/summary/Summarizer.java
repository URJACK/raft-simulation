package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;

/**
 * 总结者 用自己的方法来对整个数据进行总结统计
 * <p>
 * 在NetSimulator中进行调用
 * <pre>
 * summarizer.summarize(param);
 * </pre>
 *
 * @see NetSimulator
 */
public abstract class Summarizer {
    protected NetSimulator simulator;

    /**
     * @param simulator 网络模拟器对象引用
     */
    public Summarizer(NetSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * 总结触发动作
     *
     * @param param 触发动作参数
     */
    public abstract void summarize(String param);
}

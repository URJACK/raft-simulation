package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import com.sicnu.netsimu.core.statis.EnergyStatistician;
import com.sicnu.netsimu.core.statis.Statistician;

import java.util.*;

/**
 * 增量总结器
 * 通过summarize调用两个内置方法
 */
public abstract class IncrementalSummarizer extends Summarizer {
    public static final String OUTPUT = "OUTPUT";
    public static final String CALC = "CALC";

    /**
     * @param simulator 网络模拟器对象引用
     */
    public IncrementalSummarizer(NetSimulator simulator) {
        super(simulator);
    }

    /**
     * 首先调用processBasicCalc()
     * 如果 param == "OUTPUT" 则调用 processOutput()
     *
     * @param param 触发动作参数，用来控制是否调用 processOutput()
     */
    @Override
    public final void summarize(String param) {
        processBasicCalc();
        if (param.equals(OUTPUT)) {
            processOutput();
        }
    }

    /**
     * 当传入的param是 OUTPUT的时候，summarize() 会调用该方法。
     */
    protected abstract void processOutput();

    /**
     * 基础增量计算
     * <p>
     * 无论传入的是 CALC 还是 OUTPUT，
     * summarize都会调用该方法，计算各个数据的增量
     */
    protected abstract void processBasicCalc();
}

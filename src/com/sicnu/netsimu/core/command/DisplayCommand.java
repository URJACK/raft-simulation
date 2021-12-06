package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.ui.InfoOutputManager;
import com.sicnu.netsimu.core.NetSimulator;
import lombok.Data;

/**
 * 打印信息动作
 * 需要与ui.InfoOutputManager.java 进行耦合
 * <pre>
 *     DISPLAY_CON
 * </pre>
 * 本质上调用 InfoOutputManager 的 outputInfo 来清空还没有打印的信息
 *
 * @see InfoOutputManager
 */
@Data
public class DisplayCommand extends Command {

    /**
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      命令类型
     */
    public DisplayCommand(NetSimulator simulator, long timeStamp, String type) {
        super(simulator, timeStamp, type);
    }

    @Override
    public void work() {
        InfoOutputManager infoOutputManager = simulator.getInfoOutputManager();
        infoOutputManager.outputInfo();
    }
}

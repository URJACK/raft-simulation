package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.ui.InfoOutputManager;
import com.sicnu.netsimu.core.NetSimulator;
import lombok.Data;

/**
 * 打印信息动作
 * 需要与ui.InfoOutputManager.java 进行耦合
 */
@Data
public class DisplayCommand extends Command {

    /**
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type 命令类型
     */
    public DisplayCommand(NetSimulator simulator, long timeStamp, CommandType type) {
        super(simulator, timeStamp, type);
    }

    @Override
    public void work() {
        InfoOutputManager infoOutputManager = simulator.getInfoOutputManager();
        infoOutputManager.outputInfo();
    }
}

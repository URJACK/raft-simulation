package com.sicnu.netsimu.core.command;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.core.mote.MoteManager;
import lombok.Data;

/**
 * “启动节点”命令
 * <pre>
 * 1000, NODE_BOOT, 1
 * </pre>
 * 启动“节点1”。当节点被SHUTDOWN关闭后，可以使用它重启。
 * @see NodeShutCommand
 */
@Data
public class NodeBootCommand extends Command {
    // 节点id
    int nodeId;

    /**
     * @param simulator 仿真器对象引用
     * @param timeStamp 时间戳
     * @param type      命令类型
     * @param nodeId    节点id
     */
    public NodeBootCommand(NetSimulator simulator, long timeStamp, String type, int nodeId) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        if (moteManager.containMote(nodeId)) {
            Mote mote = moteManager.getMote(nodeId);
            if (mote == null) {
                //获取节点失败
                return;
            }
            // 每个新结点，都将触发自身的init()函数
//            mote.init();
            mote.call("init");
        }
    }
}

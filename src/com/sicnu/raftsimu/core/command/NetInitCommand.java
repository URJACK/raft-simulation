package com.sicnu.raftsimu.core.command;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.mote.Mote;
import com.sicnu.raftsimu.core.mote.MoteManager;
import lombok.Data;

/**
 * “结点网络设置”命令
 */
@Data
public class NetInitCommand extends Command {
    // 节点id
    int nodeId;
    // 操作值类型 指明是ip还是端口
    Operation operation;
    String value;

    /**
     * @param simulator 模拟器
     * @param timeStamp 时间戳
     * @param type      指令类型
     * @param nodeId    节点id
     * @param operation 操作类型，是ip还是port
     * @param value     操作值
     */
    public NetInitCommand(RaftSimulator simulator, long timeStamp, CommandType type, int nodeId, Operation operation, String value) {
        super(simulator, timeStamp, type);
        this.nodeId = nodeId;
        this.operation = operation;
        this.value = value;
    }

    @Override
    public void work() {
        MoteManager moteManager = simulator.getMoteManager();
        Mote mote = moteManager.getMote(nodeId);
        if (this.operation == Operation.PORT) {
            int port = Integer.parseInt(this.value);
            mote.listenPort(port);
        } else {
            String ip = this.value;
            mote.listenIp(ip);
        }
    }

    public enum Operation {
        IP, PORT
    }
}

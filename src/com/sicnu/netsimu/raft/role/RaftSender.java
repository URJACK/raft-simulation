package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.core.event.trans.TransmissionPacket;
import com.sicnu.netsimu.core.mote.Mote;
import com.sicnu.netsimu.raft.mote.RaftMote;
import com.sicnu.netsimu.raft.role.rpc.RPCConvert;

public class RaftSender {

    private int NODE_NUM;
    private Mote mote;

    public RaftSender(Mote mote, int NODE_NUM) {
        this.mote = mote;
        this.NODE_NUM = NODE_NUM;
    }

    /**
     * 广播一个 RPC对象
     *
     * @param rpc 实现了RPC对象转换接口的对象
     */
    public void broadCast(RPCConvert rpc) {
        for (int i = 0; i < NODE_NUM; i++) {
            if (mote.getMoteId() - 1 == i) {
                //不会发给自己
                continue;
            }
            String dstIp = RaftUtils.convertIpAddressWithMoteId(RaftMote.IP_PREFIX, i + 1);
            String selfIp = mote.getAddress(0);
            TransmissionPacket packet = new TransmissionPacket(selfIp, dstIp,
                    RaftMote.RAFT_PORT, RaftMote.RAFT_PORT, rpc.convert());
            mote.netSend(packet);
        }
    }

    /**
     * 单播一个RPC对象
     *
     * @param desMoteId 目标节点的id
     * @param rpc       要发送的RPC对象
     */
    public void uniCast(int desMoteId, RPCConvert rpc) {
        String desIp = RaftUtils.convertIpAddressWithMoteId(RaftMote.IP_PREFIX, desMoteId);
        String selfIp = mote.getAddress(0);
        TransmissionPacket packet = new TransmissionPacket(selfIp, desIp,
                RaftMote.RAFT_PORT, RaftMote.RAFT_PORT, rpc.convert());
        mote.netSend(packet);
    }
}

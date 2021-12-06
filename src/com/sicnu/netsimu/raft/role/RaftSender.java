package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.core.net.NetStack;
import com.sicnu.netsimu.core.net.mac.MACLayer;
import com.sicnu.netsimu.raft.RaftUtils;
import com.sicnu.netsimu.raft.exception.ParseException;
import com.sicnu.netsimu.raft.mote.RaftMote;
import com.sicnu.netsimu.raft.role.rpc.RPCConvert;

/**
 * Raft发送器
 */
public class RaftSender {
    /**
     * 记录的Raft节点个数
     */
    private int NODE_NUM;
    /**
     * 记录Raft节点的引用
     */
    private RaftMote mote;

    /**
     * @param mote     Raft的节点引用
     * @param NODE_NUM Raft算法节点个数
     */
    public RaftSender(RaftMote mote, int NODE_NUM) {
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
            NetStack stack = mote.getNetStack();
            String dstMac = RaftUtils.convertMACAddressWithMoteId(RaftMote.MAC_PREFIX, i + 1);
            try {
                MACLayer.Header header = new MACLayer.Header(stack.getInfo("mac"), dstMac);
                String packet = stack.convert(rpc.convert(), header);
                mote.netSend(packet);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 单播一个RPC对象
     *
     * @param desMoteId 目标节点的id
     * @param rpc       要发送的RPC对象
     */
    public void uniCast(int desMoteId, RPCConvert rpc) {
        NetStack stack = mote.getNetStack();
        String dstMac = RaftUtils.convertMACAddressWithMoteId(RaftMote.MAC_PREFIX, desMoteId);
        try {
            MACLayer.Header header = new MACLayer.Header(stack.getInfo("mac"), dstMac);
            String packet = stack.convert(rpc.convert(), header);
            mote.netSend(packet);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

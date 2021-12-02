package com.sicnu.netsimu.raft.role.rpc;

/**
 * 这代表了是一个请求类型的RPC
 * 请求类RPC都会有发送方的Id字段
 */
public interface RequestRPC {
    /**
     * @return 取得发送者的Id
     */
    int getSenderId();
}

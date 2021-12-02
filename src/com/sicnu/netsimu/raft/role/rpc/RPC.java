package com.sicnu.netsimu.raft.role.rpc;

import java.util.HashMap;

/**
 * RPC 常量类
 */
public class RPC {
    /**
     * 选举请求包 的 标识
     */
    public static final int RPC_ELECT = 0;
    /**
     * 选举请求响应包 的 标识
     */
    public static final int RPC_ELECT_RESP = 1;
    /**
     * 心跳包 的 标识
     */
    public static final int RPC_HEARTBEATS = 2;
    /**
     * 心跳响应包 的 标识
     */
    public static final int RPC_HEARTBEATS_RESP = 3;
}

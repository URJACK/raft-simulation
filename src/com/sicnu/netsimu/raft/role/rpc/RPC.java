package com.sicnu.netsimu.raft.role.rpc;

import java.util.HashMap;

public class RPC {
    public static final int RPC_ELECT = 0;
    public static final int RPC_ELECT_RESP = 1;
    public static final int RPC_HEARTBEATS = 2;
    public static final int RPC_HEARTBEATS_RESP = 3;
}

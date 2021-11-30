package com.sicnu.netsimu.raft.rpc;

import java.util.HashMap;

public class RPC {
    public static final char RPC_ELECT = 0;
    public static final char RPC_ELECT_RESP = 1;
    public static final char RPC_HEARTBEATS = 2;
    public static final char RPC_HEARTBEATS_RESP = 3;
    public static HashMap<Character, RPCConvert> map;

    static {
        map = new HashMap<>();
    }
}

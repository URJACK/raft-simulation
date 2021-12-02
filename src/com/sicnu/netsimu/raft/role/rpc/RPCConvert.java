package com.sicnu.netsimu.raft.role.rpc;

public interface RPCConvert {
    String convert();

    void parse(String str);
}

package com.sicnu.netsimu.raft.rpc;

public interface RPCConvert {
    String convert();

    void parse(String str);
}

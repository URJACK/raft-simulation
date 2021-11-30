package com.sicnu.netsimu.raft.role;

public class RaftUtils {

    public static String getIpStr(String ipPrefix, int moteId) {
        return ipPrefix + moteId;
    }
}

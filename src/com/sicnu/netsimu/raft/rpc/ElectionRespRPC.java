package com.sicnu.netsimu.raft.rpc;

public class ElectionRespRPC implements RPCConvert {
    char type;
    int term;
    int voteGranted;
    int senderId;

    public ElectionRespRPC(char type, int term, int voteGranted, int senderId) {
        this.type = type;
        this.term = term;
        this.voteGranted = voteGranted;
        this.senderId = senderId;
    }

    @Override
    public String convert() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(",");
        sb.append(term);
        sb.append(",");
        sb.append(voteGranted);
        sb.append(",");
        sb.append(senderId);
        return sb.toString();
    }

    @Override
    public void parse(String str) {
        String[] splits = str.split(",");
        if (splits.length != 4) {
            new Exception("Parse Exception the elements.length is not 4").printStackTrace();
        }
        type = splits[0].charAt(0);
        term = Integer.parseInt(splits[1]);
        voteGranted = Integer.parseInt(splits[2]);
        senderId = Integer.parseInt(splits[3]);
    }
}

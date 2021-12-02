package com.sicnu.netsimu.raft.role.rpc;

import lombok.Data;

@Data
public class ElectionRespRPC implements RPCConvert {
    int type;
    int term;
    int voteGranted;
    int senderId;

    public ElectionRespRPC(int type, int term, int voteGranted, int senderId) {
        this.type = type;
        this.term = term;
        this.voteGranted = voteGranted;
        this.senderId = senderId;
    }

    public ElectionRespRPC(String data) {
        parse(data);
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
        type = Integer.parseInt(splits[0]);
        term = Integer.parseInt(splits[1]);
        voteGranted = Integer.parseInt(splits[2]);
        senderId = Integer.parseInt(splits[3]);
    }
}

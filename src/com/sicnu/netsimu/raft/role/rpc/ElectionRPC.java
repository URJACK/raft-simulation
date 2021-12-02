package com.sicnu.netsimu.raft.role.rpc;

import lombok.Data;

@Data
public class ElectionRPC implements RPCConvert, RequestRPC {
    int type;
    int term;
    int candidateId;
    int lastLogIndex;
    int lastLogTerm;

    public ElectionRPC(String compressedData) {
        parse(compressedData);
    }

    public ElectionRPC(int type, int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        this.type = type;
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String convert() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(",");
        sb.append(term);
        sb.append(",");
        sb.append(candidateId);
        sb.append(",");
        sb.append(lastLogIndex);
        sb.append(",");
        sb.append(lastLogTerm);
        return sb.toString();
    }

    @Override
    public void parse(String str) {
        String[] splits = str.split(",");
        if (splits.length != 5) {
            new Exception("Parse Exception the elements.length is not 4").printStackTrace();
        }
        type = Integer.parseInt(splits[0]);
        term = Integer.parseInt(splits[1]);
        candidateId = Integer.parseInt(splits[2]);
        lastLogIndex = Integer.parseInt(splits[3]);
        lastLogTerm = Integer.parseInt(splits[4]);
    }

    @Override
    public int getSenderId() {
        return candidateId;
    }
}

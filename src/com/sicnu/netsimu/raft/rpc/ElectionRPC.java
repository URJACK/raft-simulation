package com.sicnu.netsimu.raft.rpc;

public class ElectionRPC implements RPCConvert {
    char type;
    int candidateId;
    int lastLogIndex;
    int lastLogTerm;

    public ElectionRPC(String compressedData) {
        parse(compressedData);
    }

    public ElectionRPC(char type, int candidateId, int lastLogIndex, int lastLogTerm) {
        this.type = type;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String convert() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
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
        if (splits.length != 4) {
            new Exception("Parse Exception the elements.length is not 4").printStackTrace();
        }
        type = splits[0].charAt(0);
        candidateId = Integer.parseInt(splits[1]);
        lastLogIndex = Integer.parseInt(splits[2]);
        lastLogTerm = Integer.parseInt(splits[3]);
    }

    @Override
    public String toString() {
        return "ElectionRPC{" +
                "type=" + type +
                ", candidateId=" + candidateId +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                '}';
    }
}

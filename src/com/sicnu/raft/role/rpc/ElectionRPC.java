package com.sicnu.raft.role.rpc;

import lombok.Data;

/**
 * 节点发送选举请求会发送的RPC
 */
@Data
public class ElectionRPC implements RPCConvert, SenderRPC {
    /**
     * RPC 类型字段
     */
    int type;
    /**
     * 当前节点想要竞选的任期号
     */
    int term;
    /**
     * 当前节点自身的节点Id号
     */
    int candidateId;
    /**
     * 当前节点最后一条日志的 index
     */
    int lastLogIndex;
    /**
     * 当前节点最后一条日志的 term
     */
    int lastLogTerm;

    /**
     * @param compressedData 实际传输过程中使用的压缩字符串
     */
    public ElectionRPC(String compressedData) {
        parse(compressedData);
    }

    /**
     * 构造函数
     *
     * @param type         RPC类型字段
     * @param term         节点想要竞选任期号
     * @param candidateId  参选节点自身的Id号
     * @param lastLogIndex 参选节点最后一条日志的index
     * @param lastLogTerm  参选节点最后一条日志的term
     */
    public ElectionRPC(int type, int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        this.type = type;
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    /**
     * 将自身的数据转为字符串，用于输出传输
     */
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

    /**
     * 解析 压缩字符串 ，并将数据放入自己的成员变量中
     *
     * @param str 压缩字符串
     */
    @Override
    public void parse(String str) {
        String[] splits = str.split(",");
        if (splits.length != 5) {
            new Exception("Parse Exception the elements.length is not 4").printStackTrace();
            return;
        }
        type = Integer.parseInt(splits[0]);
        term = Integer.parseInt(splits[1]);
        candidateId = Integer.parseInt(splits[2]);
        lastLogIndex = Integer.parseInt(splits[3]);
        lastLogTerm = Integer.parseInt(splits[4]);
    }

    /**
     * @return 取得发送者的Id
     */
    @Override
    public int getSenderId() {
        return candidateId;
    }
}

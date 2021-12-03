package com.sicnu.netsimu.raft.role.rpc;

import lombok.Data;

/**
 * 节点选举响应的RPC
 */
@Data
public class ElectionRespRPC implements RPCConvert {
    /**
     * RPC 类型字段
     */
    int type;
    /**
     * 当前节点回复的任期号（可以比发送方的任期更高）
     */
    int term;
    /**
     * 是否同意投票
     */
    int voteGranted;
    /**
     * 发送者Id
     */
    int senderId;

    /**
     * @param type        RPC类型字段
     * @param term        节点回复的任期号
     * @param voteGranted 是否同意投票
     * @param senderId    发送者的Id
     */
    public ElectionRespRPC(int type, int term, int voteGranted, int senderId) {
        this.type = type;
        this.term = term;
        this.voteGranted = voteGranted;
        this.senderId = senderId;
    }

    /**
     * @param compressedData 实际传输过程中使用的压缩字符串
     */
    public ElectionRespRPC(String compressedData) {
        parse(compressedData);
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
        sb.append(voteGranted);
        sb.append(",");
        sb.append(senderId);
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
        if (splits.length != 4) {
            new Exception("Parse Exception the elements.length is not 4").printStackTrace();
            return;
        }
        type = Integer.parseInt(splits[0]);
        term = Integer.parseInt(splits[1]);
        voteGranted = Integer.parseInt(splits[2]);
        senderId = Integer.parseInt(splits[3]);
    }
}

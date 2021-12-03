package com.sicnu.netsimu.raft.role.rpc;

import com.sicnu.netsimu.raft.annotation.AllowNull;
import com.sicnu.netsimu.raft.exception.ParseException;
import com.sicnu.netsimu.raft.role.log.RaftLogItem;
import lombok.Data;

/**
 * 参数个数为 5个 或者 10个
 * 前5个参数为： type, term, leaderId, prevIndex, prevTerm
 * 后5个参数为： logItem 的属性
 */
@Data
public class HeartBeatsRPC implements RPCConvert, SenderRPC {
    /**
     * RPC 类型字段
     */
    int type;
    /**
     * 当前Leader节点的任期号
     */
    int term;
    /**
     * 当前Leader自身的节点Id号
     */
    int leaderId;
    /**
     * 最新日志之前的 index
     */
    int prevIndex;
    /**
     * 最新日志之前的 term
     */
    int prevTerm;
    //    int hasEntry;
    RaftLogItem logItem;

    /**
     * @param compressedData 实际传输过程中使用的压缩字符串
     */
    public HeartBeatsRPC(String compressedData) {
        parse(compressedData);
    }

    /**
     * 该构造函数中，最后一个参数 LogItem 是可以为空的
     * <p>
     * 如果为空，我们就按照5个参数的标准进行 压缩 与 解析。
     * <p>
     * 反之不为空，我们就按照10个参数（LogItem自身有5个参数）的标准进行 压缩 与 解析
     *
     * @param type      RPC类型字段
     * @param term      Leader节点自身的任期号
     * @param leaderId  Leader节点自身的Id号
     * @param prevIndex 最新日志的index
     * @param prevTerm  最新日志的term
     * @param logItem   心跳包可以传入logItem
     * @see RaftLogItem
     */
    public HeartBeatsRPC(int type, int term, int leaderId, int prevIndex, int prevTerm, @AllowNull RaftLogItem logItem) {
        this.type = type;
        this.term = term;
        this.leaderId = leaderId;
        this.prevIndex = prevIndex;
        this.prevTerm = prevTerm;
        this.logItem = logItem;
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
        sb.append(leaderId);
        sb.append(",");
        sb.append(prevIndex);
        sb.append(",");
        sb.append(prevTerm);
        if (logItem != null) {
            sb.append(",");
            sb.append(logItem.getIndex());
            sb.append(",");
            sb.append(logItem.getTerm());
            sb.append(",");
            sb.append(logItem.getOperation());
            sb.append(",");
            sb.append(logItem.getKey());
            sb.append(",");
            sb.append(logItem.getValue());
        }
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
        if (splits.length == 10 || splits.length == 5) {
            //共有部分
            type = Integer.parseInt(splits[0]);
            term = Integer.parseInt(splits[1]);
            leaderId = Integer.parseInt(splits[2]);
            prevIndex = Integer.parseInt(splits[3]);
            prevTerm = Integer.parseInt(splits[4]);
            if (splits.length == 10) {
                //如果 Heartbeats 有数据部分 logItem
                int logIndex = Integer.parseInt(splits[5]);
                int logTerm = Integer.parseInt(splits[6]);
                logItem = new RaftLogItem(logIndex, logTerm,
                        splits[7], splits[8], splits[9]);
            }
        } else {
            new ParseException("Parse Exception the elements.length is not 10").printStackTrace();
        }
    }

    /**
     * @return 取得发送者的Id
     */
    @Override
    public int getSenderId() {
        return leaderId;
    }
}

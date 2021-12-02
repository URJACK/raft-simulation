package com.sicnu.netsimu.raft.role.rpc;

import com.sicnu.netsimu.raft.exception.ParseException;
import com.sicnu.netsimu.raft.role.RaftLogItem;
import lombok.Data;

/**
 * 参数个数为 5个 或者 10个
 * 前5个参数为： type, term, leaderId, prevIndex, prevTerm
 * 后5个参数为： logItem 的属性
 */
@Data
public class HeartbeatsRPC implements RPCConvert, RequestRPC {
    int type;
    int term;
    int leaderId;
    int prevIndex;
    int prevTerm;
    //    int hasEntry;
    RaftLogItem logItem;

    public HeartbeatsRPC(String compressedData) {
        parse(compressedData);
    }

    public HeartbeatsRPC(int type, int term, int leaderId, int prevIndex, int prevTerm, RaftLogItem logItem) {
        this.type = type;
        this.term = term;
        this.leaderId = leaderId;
        this.prevIndex = prevIndex;
        this.prevTerm = prevTerm;
        this.logItem = logItem;
    }

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

    @Override
    public int getSenderId() {
        return leaderId;
    }
}

package com.sicnu.raft.role.rpc;

import com.sicnu.netsimu.exception.ParseException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeartBeatsRespRPC implements RPCConvert, SenderRPC {
    /**
     * RPC 类型字段
     */
    int type;
    /**
     * 当前Leader节点的任期号
     */
    int term;
    /**
     * 是否和Leader的数据项完成了匹配
     */
    int isMatched;
    /**
     * 与Leader匹配的日志项号，
     * 这意味着从 <strong> 第0条（占位条）</strong> 到 <strong> 第matchIndex条（包括） </strong>
     * 都是日志匹配的
     */
    int matchIndex;
    /**
     * 自身回复Leader的时候，Leader会依据该Id，
     * 对相应的 nextIndexes 和 matchIndexes 进行修改。
     */
    int senderId;

    public HeartBeatsRespRPC(String data) {
        parse(data);
    }

    /**
     * 将自身的数据转为 压缩字符串 ，用于输出传输
     */
    @Override
    public String convert() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append(',');
        sb.append(term);
        sb.append(',');
        sb.append(isMatched);
        sb.append(',');
        sb.append(matchIndex);
        sb.append(',');
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
        if (splits.length != 5) {
            new ParseException("Parse Exception the elements.length is not 5").printStackTrace();
            return;
        }
        type = Integer.parseInt(splits[0]);
        term = Integer.parseInt(splits[1]);
        isMatched = Integer.parseInt(splits[2]);
        matchIndex = Integer.parseInt(splits[3]);
        senderId = Integer.parseInt(splits[4]);
    }
}

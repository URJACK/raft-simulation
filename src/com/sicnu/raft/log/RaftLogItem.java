package com.sicnu.raft.log;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Raft日志，单项Log
 */
@Data
@AllArgsConstructor
public class RaftLogItem {
    /**
     * 日志的index号
     */
    int index;
    /**
     * 日志记录的term号
     */
    int term;
    /**
     * 日志记录下的操作
     */
    String operation;
    /**
     * 日志要操作的key
     */
    String key;
    /**
     * 日志要操作的value
     */
    String value;
}

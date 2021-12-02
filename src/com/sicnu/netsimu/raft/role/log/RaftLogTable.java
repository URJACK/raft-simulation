package com.sicnu.netsimu.raft.role.log;

import java.util.ArrayList;

/**
 * Raft表
 * item的编号是从1开始的
 */
public class RaftLogTable {
    /**
     * 日志的存储结构
     */
    ArrayList<RaftLogItem> items;

    public RaftLogTable() {
        this.items = new ArrayList<>();
    }

    /**
     * @return 最后一个item的index
     */
    public int getLastLogIndex() {
        if (items.isEmpty()) {
            return 0;
        }
        return items.get(items.size() - 1).index;
    }

    /**
     * @return 最后一个item的term
     */
    public int getLastLogTerm() {
        if (items.isEmpty()) {
            return 0;
        }
        return items.get(items.size() - 1).term;
    }

}

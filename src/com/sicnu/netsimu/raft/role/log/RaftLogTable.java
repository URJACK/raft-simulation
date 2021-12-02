package com.sicnu.netsimu.raft.role.log;

import java.util.ArrayList;

/**
 * Raft表
 * item的编号是从1开始的
 */
public class RaftLogTable {
    /**
     * 日志的存储结构
     * <p>
     * items 的 index 是从1 开始的
     */
    ArrayList<RaftLogItem> items;

    /**
     * 日志个数
     */
    int n;

    public RaftLogTable() {
        this.items = new ArrayList<>();
        //添加一条占位用的空数据 让整个节点的访问空间锁定在 [1, n] 闭区间
        this.items.add(new RaftLogItem(0, 0, "", "", ""));
        this.n = 0;
    }

    /**
     * @return 最后一个item的index
     */
    public int getLastLogIndex() {
        if (n == 0) {
            return 0;
        }
        return items.get(n).index;
    }

    /**
     * @return 最后一个item的term
     */
    public int getLastLogTerm() {
        if (n == 0) {
            return 0;
        }
        return items.get(n).term;
    }


    /**
     * 根据index下标来读取到日志对象
     * <p>
     * RaftLogItem item = raftLogTable.getLogByIndex(0);
     *
     * @param index 下标 --> [1, n] 为闭区间
     * @return 日志对象
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see RaftLogItem
     */
    public RaftLogItem getLogByIndex(int index) throws IndexOutOfBoundsException {
        if (index > n || index < 1) {
            throw new IndexOutOfBoundsException("index 不符合要求 需要在 [1,n] 闭区间之间");
        }
        return items.get(index);
    }
}

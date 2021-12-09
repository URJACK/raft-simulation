package com.sicnu.raft.role.log;

import com.sicnu.netsimu.exception.ParseException;
import com.sicnu.raft.role.RaftRoleLogic;

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
    /**
     * 日志确认编号
     */
    int commitIndex;

    public RaftLogTable() {
        this.items = new ArrayList<>();
        clear();
    }

    /**
     * @return 最后一个item的index
     */
    public int getLastLogIndex() {
        return items.get(n).index;
    }

    /**
     * @return 最后一个item的term
     */
    public int getLastLogTerm() {
        return items.get(n).term;
    }


    /**
     * 根据index下标来读取到日志对象
     * <p>
     * RaftLogItem item = raftLogTable.getLogByIndex(0);
     *
     * @param index 下标 --> [0, n] 为闭区间
     * @return 日志对象
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see RaftLogItem
     */
    public RaftLogItem getLogByIndex(int index) throws IndexOutOfBoundsException {
        if (index > n || index < 0) {
            throw new IndexOutOfBoundsException("index " + index + " 不符合要求 需要在 [1,n] 闭区间之间");
        }
        return items.get(index);
    }

    /**
     * 末尾添加一条日志（不带index），
     * 触发方式如下：
     * <pre>
     * RaftLogTable table = ...;
     * table.addLog(operationType, key, value);
     * </pre>
     * 本身是作为一个调用者，实际添加的方法，写在 addLog( RaftLogItem, int ) 中
     *
     * @param operation 操作类型
     * @param key       操作键
     * @param value     操作值
     * @see com.sicnu.raft.mote.RaftMote
     */
    public void addLogInLast(String operation, String key, String value, int term) {
        // 新增日志条目的index 肯定比当前的n 要多1一个
        RaftLogItem item = new RaftLogItem(this.n + 1, term, operation, key, value);
        this.addLog(item, this.n + 1);
    }

    /**
     * 添加一条日志，触发方式如下：
     * <pre>
     *     RaftLogTable table = ...;
     *     RaftLogItem log = ...;
     *     table.addLog(log)
     * </pre>
     * 会受到<strong>addLog(String operation, String key, String value, int term)</strong>的调用。
     * <p>
     * 同时也可以被外部调用的。
     * 该方法会对ArrayList的多余对象进行清空
     * 进而自动维护n这个变量
     *
     * @param logItem 被添加的日志对象
     * @param index   添加对象的index
     * @throws IndexOutOfBoundsException index可以属于[1,n + 1] 之间的所有值
     */
    public void addLog(RaftLogItem logItem, int index) {
        if (index < 1) {
            throw new IndexOutOfBoundsException("index 的范围必须不可以为 0");
        }
        if (index <= n) {
            /*
            如果加的日志在这个范围内，我们需要清理掉原本的 [index, n] 之间的日志
            在完成清理后，items.length() 应该等于该 index
            eg:
                n = 5 : 因为有空日志的存在，意味着 items.size() == 6
                index = 3 : 我们删除掉 [3,5] 留下了 [0, 2] --> size == index = 3
             */
            if (items.size() != n + 1) {
                new ParseException("RaftLog's n 与 它持有的items.length() 不匹配").printStackTrace();
                return;
            }
            // 清空掉index后面的元素
            while (items.size() > index) {
                items.remove(items.size() - 1);
            }
            // 清理元素后，往item增加该元素
            this.n = index;
            items.add(logItem);
        } else {
            /*
            如果加的日志不属于本来的范围内，
            我们对index也有要求：index = n + 1
            因为我们显然不能允许日志表中出现空日志
             */
            if (index != n + 1) {
                // 此时意味着 index 已经比 n + 1 还要大，插入会有空日志
                throw new IndexOutOfBoundsException("index is out of range of n + 1");
            }
            this.n++;
            this.items.add(logItem);
            if (logItem.getIndex() != this.n) {
                new ParseException("lastLogIndex is not equals with the n").printStackTrace();
            }
        }
    }

    /**
     * 清空日志表
     * 调用方式如下：
     * <pre>
     *     RaftLogTable table = ...;
     *     table.clear()
     * </pre>
     * 可能会被RaftRole进行调用
     *
     * @see RaftRoleLogic
     */
    public void clear() {
        this.n = 0;
        this.commitIndex = 0;
        this.items.clear();
        //添加一条占位用的空数据 让整个节点的访问空间锁定在 [1, n] 闭区间
        this.items.add(new RaftLogItem(0, 0, "", "", ""));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            sb.append(items.get(i).toString());
        }
        return "RaftLogTable{" +
                sb.toString() +
                ", n=" + n +
                '}';
    }

    /**
     * 删除对应index的Log日志
     * <pre>
     *     RaftLogTable table = ...
     *     table.deleteAt(4)
     * </pre>
     * 一般是通过BasicRaftRole进行调用
     * <p>
     * 需要注意，delete的下标 -> [1,n]
     * <p>
     * 但是getLogByIndex的下标 -> [0,n]
     * 因为0号日志是占位日志。
     *
     * @param deleteIndex 删除的日志下标
     * @see RaftRoleLogic
     */
    public void deleteAt(int deleteIndex) {
        if (deleteIndex > 0 && deleteIndex <= n) {
            RaftLogItem remove = this.items.remove(deleteIndex);
            this.n--;
        } else {
            throw new IndexOutOfBoundsException("当前删除的index: " + deleteIndex +
                    " RaftLogTable.deleteAt 的 访问下标需要控制在 1 ~ n 之间");
        }
    }

    /**
     * 获取日志表长度
     * <p>
     * 我们的日志表长度为 n 的时候，实际存储结构的长度为 n + 1，因为包括了一个占位日志
     *
     * @return 日志表的长度（不包含占位日志）
     */
    public int getLength() {
        return n;
    }
}

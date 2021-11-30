package com.sicnu.netsimu.raft.role;

import com.sicnu.netsimu.raft.command.RaftOpCommand;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RaftLogItem {
    int index;
    int term;
    String operation;
    String key;
    String value;
}

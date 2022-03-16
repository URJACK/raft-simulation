package com.sicnu;

import com.sicnu.netsimu.ui.scene.LinearAddNodeScene;
import com.sicnu.netsimu.ui.scene.Scene;
import com.sicnu.raft.node.RaftNode;

public class SceneRunner {
    public static void main(String[] args) {
        String filepath = "resources/commands_raft_4_op.txt";
        int nodeNum = 10;
        Scene scene = new LinearAddNodeScene(filepath);
        String[] nodeParam = {String.valueOf(nodeNum)};
        scene.generateCommands(new LinearAddNodeScene.LinearConfig(0, 0, 10, 0, 500, 100,
                RaftNode.class, nodeParam));
        scene.output(false);
    }
}

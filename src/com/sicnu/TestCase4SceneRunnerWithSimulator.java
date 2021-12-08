package com.sicnu;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.CommandTranslator;
import com.sicnu.netsimu.core.utils.NetSimulationRandom;
import com.sicnu.netsimu.ui.scene.LinearAddNodeScene;
import com.sicnu.netsimu.ui.scene.Scene;
import com.sicnu.raft.command.RaftCommandTranslator;
import com.sicnu.raft.mote.RaftMote;
import com.sicnu.raft.ui.RaftCalculateInPostEventInterceptor;
import com.sicnu.raft.ui.RaftCalculateInPreEventInterceptor;
import com.sicnu.raft.ui.RaftSummarizer;

public class TestCase4SceneRunnerWithSimulator {
    public static void main(String[] args) {
        try {
            String filepath = "resources/commands_raft_4_op.txt";
            int nodeNum = 10;
            sceneCreate(filepath, nodeNum);
            simulateRun(filepath, nodeNum, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simulateRun(String filePath, int nodeNum, long endTime) throws Exception {
        //Raft 节点个数
        NetSimulationRandom.setNetRandomSeed((long) (Math.random() * 100000));
        CommandTranslator translator = new RaftCommandTranslator();
        //设置仿真器终止时间
        NetSimulator simulator = new NetSimulator(endTime);
        //读取命令，并装配命令 filePath路径的文件，是之前通过Scene创建的
        translator.equipSimulator(simulator);
        translator.read(filePath);
        simulator.analysis(translator);
        //装配Raft总结器 可以统计各种数据
        RaftSummarizer raftSummarizer = new RaftSummarizer(simulator, nodeNum);
        simulator.equipSummarizer(raftSummarizer);
        //装配两个事件拦截器，计算各类指标，方便最终总结器统计出结果
        //事件开始之前，缓存下可能操作的节点对象
        simulator.setPreEventInterceptor(new RaftCalculateInPreEventInterceptor(simulator, raftSummarizer));
        //每次事件结束后，它会计算Raft相关的指标
        simulator.setPostEventInterceptor(new RaftCalculateInPostEventInterceptor(simulator, raftSummarizer));
        simulator.run();
        System.out.println("NetRandomSeed = " + NetSimulationRandom.getNetRandomSeed());
    }

    private static void sceneCreate(String filepath, int nodeNum) {
        Scene scene = new LinearAddNodeScene(filepath);
        String[] nodeParam = {String.valueOf(nodeNum)};
        scene.generateCommands(new LinearAddNodeScene.LinearConfig(0, 0, 10, 0, 500, 100,
                RaftMote.class, nodeParam));
        scene.output(false);
    }
}

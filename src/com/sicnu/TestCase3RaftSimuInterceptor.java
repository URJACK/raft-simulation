package com.sicnu;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.CommandTranslator;
import com.sicnu.netsimu.core.utils.NetSimulationRandom;
import com.sicnu.raft.command.RaftCommandTranslator;
import com.sicnu.raft.ui.RaftCalculateInPostEventInterceptor;
import com.sicnu.raft.ui.RaftCalculateInPreEventInterceptor;
import com.sicnu.raft.ui.RaftSummarizer;


public class TestCase3RaftSimuInterceptor {
    public static void main(String[] args) {
        try {
//            CommandTranslator translator = new BasicCommandTranslator();
            //Raft 节点个数
            int n = 3;
            NetSimulationRandom.setNetRandomSeed((long) (Math.random() * 100000));
//            NetSimulationRandom.setNetRandomSeed(70);
            NetSimulator simulator = new NetSimulator(3000);
            CommandTranslator translator = new RaftCommandTranslator();
            translator.equipSimulator(simulator);
            translator.read("resources/commands_raft_3_op.txt");
//            translator.read("resources/commands_1.txt");
            simulator.analysis(translator);
            //装配Raft总结器
            RaftSummarizer raftSummarizer = new RaftSummarizer(simulator, n);
            simulator.equipSummarizer(raftSummarizer);
            //装配两个事件拦截器
            //事件开始之前，缓存下可能操作的节点对象
            simulator.setPreEventInterceptor(new RaftCalculateInPreEventInterceptor(simulator, raftSummarizer));
            //每次事件结束后，它会计算Raft相关的指标
            simulator.setPostEventInterceptor(new RaftCalculateInPostEventInterceptor(simulator, raftSummarizer));
            //开始仿真
            simulator.run();
            //运行完成后，打印随机数种子
            System.out.println("NetRandomSeed = " + NetSimulationRandom.getNetRandomSeed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

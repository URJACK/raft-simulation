package com.sicnu;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.BasicCommandTranslator;
import com.sicnu.netsimu.core.command.CommandTranslator;
import com.sicnu.netsimu.core.utils.NetSimulationRandom;
import com.sicnu.netsimu.ui.summary.BasicTotalSummarizer;

public class TestCase5MacCollisionTest {
    public static void main(String[] args) {
        try {
            String filepath = "resources/commands_raft_mac_test_op.txt";
            simulateRun(filepath, 5000L, 48881L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simulateRun(String filePath, long endTime, Long seed) throws Exception {
        if (seed == null) {
            //Raft 节点个数
            NetSimulationRandom.setNetRandomSeed((long) (Math.random() * 100000));
        } else {
            NetSimulationRandom.setNetRandomSeed(seed);
        }
        CommandTranslator translator = new BasicCommandTranslator();
        //设置仿真器终止时间
        NetSimulator simulator = new NetSimulator(endTime);
        //读取命令，并装配命令 filePath路径的文件，是之前通过Scene创建的
        translator.equipSimulator(simulator);
        translator.read(filePath);
        simulator.analysis(translator);
        //装配Raft总结器 可以统计各种数据
        simulator.equipSummarizer(new BasicTotalSummarizer(simulator));
        simulator.run();
        System.out.println("NetRandomSeed = " + NetSimulationRandom.getNetRandomSeed());
    }
}

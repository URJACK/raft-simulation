package com.sicnu;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.command.CommandTranslator;
import com.sicnu.netsimu.core.utils.NetsimuRandom;
import com.sicnu.netsimu.ui.summary.IncrementalSummarizerWithConsole;
import com.sicnu.netsimu.raft.command.RaftCommandTranslator;

public class Main {

    public static void main(String[] args) {
//        CommandTranslator translator = new BasicCommandTranslator();
        NetsimuRandom.setRandomSeed(25);
        CommandTranslator translator = new RaftCommandTranslator();
        //新增了endTime属性， 仿真器超过该时间后，就会停止仿真
        NetSimulator simulator = new NetSimulator(3000);
        translator.equipSimulator(simulator);
        translator.read("resources/commands_raft_2_op.txt");
        simulator.analysis(translator);
        simulator.equipSummarizer(new IncrementalSummarizerWithConsole(simulator));
//        simulator.equipSummarizer(new IncrementalSummarizerWithExcel(simulator, "output/summary.xlsx"));
        simulator.run();
    }
}

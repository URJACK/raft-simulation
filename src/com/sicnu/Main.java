package com.sicnu;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.ui.CommandTranslator;

public class Main {

    public static void main(String[] args) {
        CommandTranslator translator = new CommandTranslator();
        //新增了endTime属性， 仿真器超过该时间后，就会停止仿真
        NetSimulator simulator = new NetSimulator(3000);
        translator.equipSimulator(simulator);
        translator.read("resources/commands.txt");
        simulator.analysis(translator);
        simulator.run();
    }
}

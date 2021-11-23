package com.sicnu;

import com.sicnu.raftsimu.core.RaftSimulator;
import com.sicnu.raftsimu.core.event.EventManager;
import com.sicnu.raftsimu.core.event.trans.TransmissionManager;
import com.sicnu.raftsimu.ui.CommandTranslator;
import jdk.swing.interop.SwingInterOpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        CommandTranslator translator = new CommandTranslator();
//        System.out.println(translator);
        RaftSimulator simulator = new RaftSimulator();
        translator.equipSimulator(simulator);
        translator.read("resources/commands.txt");
        simulator.analysis(translator);
        simulator.run();
    }
}

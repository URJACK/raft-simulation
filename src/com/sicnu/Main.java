package com.sicnu;

import com.sicnu.raftsimu.core.RaftSimulator;
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
        translator.read("resources/commands.txt");
        System.out.println(translator.getCommands().size());
        System.out.println(translator);
    }
}

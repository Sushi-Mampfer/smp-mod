package com.eu.sushi.smp.commands;

import com.eu.sushi.smp.Smp;

public class SmpCommands {
    public static void initialize() {
        NoNether.initialize();
        NoEnd.initialize();
        if (Smp.config.noSleep) NoSleep.initialize();
    }
}

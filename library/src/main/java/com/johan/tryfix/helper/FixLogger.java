package com.johan.tryfix.helper;

/**
 * Created by johan on 2019/3/15.
 */

public class FixLogger {

    private static boolean debug = false;

    public static void debug() {
        debug = true;
    }

    public static void log(String content) {
        if (debug) {
            System.err.println("[try fix] " + content);
        }
    }

}

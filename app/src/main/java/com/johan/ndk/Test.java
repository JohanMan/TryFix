package com.johan.ndk;

/**
 * Created by johan on 2019/3/15.
 */

public class Test {

    static {
        System.loadLibrary("test-lib");
    }

    public static native String getInfo();

}

package com.johan.tryfix.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.johan.tryfix.helper.FixLogger;
import com.johan.tryfix.helper.VersionManager;

/**
 *
 * Created by johan on 2019/3/15.
 *
 * 注意：务必要(静态/动态)注册该广播 因为修复成功后在广播记录修复版本号
 *
 * 使用实例：见 Demo
 *
 */

public abstract class FixReceiver extends BroadcastReceiver {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FIELD_ERROR = 1;
    public static final int CODE_DOWNLOAD_ERROR = 2;
    public static final int CODE_UNZIP_ERROR = 3;
    public static final int CODE_CLASS_ERROR = 4;
    public static final int CODE_RESOURCE_ERROR = 5;
    public static final int CODE_SO_ERROR = 6;

    private static final String ACTION_FIX = "fix_action";
    private static final String FIELD_VERSION = "version_field";
    private static final String FIELD_CODE = "code_field";
    private static final String FIELD_MESSAGE = "message_field";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        int version = intent.getIntExtra(FIELD_VERSION, -1);
        int code = intent.getIntExtra(FIELD_CODE, -1);
        String message = intent.getStringExtra(FIELD_MESSAGE);
        FixLogger.log("修复结果 : " + "版本" + version + ", 修复码" + code + ", 信息" + message);
        if (code == CODE_SUCCESS) {
            VersionManager.setVersion(context, version);
        }
        onReceiveResult(version, code, message);
    }

    protected abstract void onReceiveResult(int version, int code, String message);

    /**
     * 动态注册广播
     * @param context
     * @param receiver
     */
    public static void register(Context context, FixReceiver receiver) {
        IntentFilter filter = new IntentFilter(ACTION_FIX);
        context.registerReceiver(receiver, filter);
    }

    /**
     * 动态解除广播
     * @param context
     * @param receiver
     */
    public static void unregister(Context context, FixReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    /**
     * 广播修复结果
     * @param context
     * @param version
     * @param code
     * @param message
     */
    public static void broadcast(Context context, int version, int code, String message) {
        Intent intent = new Intent(ACTION_FIX);
        intent.putExtra(FIELD_CODE, code);
        intent.putExtra(FIELD_VERSION, version);
        intent.putExtra(FIELD_MESSAGE, message);
        context.sendBroadcast(intent);
    }

}

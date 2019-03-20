package com.johan.tryfix.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.johan.tryfix.helper.FixDownloader;
import com.johan.tryfix.helper.FixFileHelper;
import com.johan.tryfix.helper.FixLogger;

import java.io.File;

/**
 *
 * Created by johan on 2019/3/15.
 *
 * 注意：务必要在 AndroidManifest 文件注册 最好放在单独进程
 *
 * 使用实例：<service android:name="com.johan.tryfix.service.FixService" android:process=":patch" />
 *
 */

public class FixService extends IntentService {

    private static final String FIELD_URL = "url_field";
    private static final String FIELD_VERSION = "version_field";

    public FixService() {
        super("TryFix");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        int version = intent.getIntExtra(FIELD_VERSION, -1);
        if (version == -1) {
            FixReceiver.broadcast(this, -1, FixReceiver.CODE_FIELD_ERROR, "version字段为空");
            return;
        }
        String url = intent.getStringExtra(FIELD_URL);
        if (TextUtils.isEmpty(url)) {
            FixReceiver.broadcast(this, version, FixReceiver.CODE_FIELD_ERROR, "url字段为空");
            return;
        }
        fix(url, version);
    }

    /**
     * 开始修复
     * @param url
     * @param version
     */
    private void fix(String url, int version) {
        String patchName = getPatchName(url);
        // fix/temp
        String tempDirectory = FixFileHelper.getTempDirectory(this);
        /** 1.先清空fix/temp文件夹 */
        FixFileHelper.clearDirectory(tempDirectory);
        // fix/temp/xx.apk
        String patchPath = tempDirectory + File.separator + patchName;
        /** 2.下载补丁 */
        if (!FixDownloader.download(url, patchPath)) {
            FixReceiver.broadcast(this, version, FixReceiver.CODE_DOWNLOAD_ERROR, "补丁文件下载失败");
            return;
        }
        FixLogger.log("下载补丁成功");
        /** 3.解压到fix/temp文件夹 */
        try {
            // fix/temp/xx.apk解压到fix/temp文件夹
            FixFileHelper.unzip(patchPath, tempDirectory);
        } catch (Exception e) {
            e.printStackTrace();
            FixReceiver.broadcast(this, version, FixReceiver.CODE_UNZIP_ERROR, "补丁文件解压失败");
            FixFileHelper.clearDirectory(FixFileHelper.getPrivateDirectory(this));
            return;
        } finally {
            // 删除补丁
            FixFileHelper.deleteFile(patchPath);
        }
        FixLogger.log("解压补丁成功");
        /** 4.1类修复 */
        if (FixFileHelper.exist(FixFileHelper.getTempDexPath(this))) {
            try {
                // fix/temp/classes.dex复制到fix/classes.dex
                FixFileHelper.copyFile(FixFileHelper.getTempDexPath(this), FixFileHelper.getDexPath(this));
                // 删除优化ODex文件 为了能自动重新生成新的优化ODex文件
                FixFileHelper.deleteDirectory(FixFileHelper.getODexFileDirectory(this));
            } catch (Exception e) {
                e.printStackTrace();
                FixReceiver.broadcast(this, version, FixReceiver.CODE_CLASS_ERROR, "修复类失败");
                FixFileHelper.clearDirectory(FixFileHelper.getPrivateDirectory(this));
            }
            FixLogger.log("修复类成功");
        }
        /** 4.2资源修复 */
        if (FixFileHelper.exist(FixFileHelper.getTempResPath(this)) || FixFileHelper.exist(FixFileHelper.getTempArscPath(this))) {
            String apkPath = FixFileHelper.getSourceApkPath(this);
            String apkDirectory = FixFileHelper.getSourceApkDirectory(this);
            try {
                // 将源APK文件解压到fix/apk文件夹
                FixFileHelper.unzip(apkPath, apkDirectory);
                // 将fix/temp/res代替fix/apk/res
                // 将fix/temp/classes.dex代替fix/apk/classes.dex
                // 将fix/temp/assets代替fix/apk/assets
                // 将fix/temp/resources.arsc代替fix/apk/resources.arsc
                // 其实就是将fix/temp文件代替fix/apk文件 这也是这种资源修复方案的败笔 目前还做不到Sophix
                FixFileHelper.replace(tempDirectory, apkDirectory);
                // 将fix/apk文件压缩为新的资源文件
                FixFileHelper.zip(apkDirectory, FixFileHelper.getResourcePath(this));
                // 删除fix/apk文件夹
                FixFileHelper.deleteDirectory(apkDirectory);
            } catch (Exception e) {
                e.printStackTrace();
                FixReceiver.broadcast(this, version, FixReceiver.CODE_RESOURCE_ERROR, "修复类失败");
                FixFileHelper.clearDirectory(FixFileHelper.getPrivateDirectory(this));
            }
            FixLogger.log("修复资源成功");
        }
        /** 4.3库修复 */
        if (FixFileHelper.exist(FixFileHelper.getTempSoDirectory(this))) {
            try {
                // 将fix/temp/lib文件夹所有文件拷贝到私有目录中
                FixFileHelper.copyDirectory(FixFileHelper.getTempSoDirectory(this), FixFileHelper.getSoPath(this));
                // 删除fix/temp文件夹
                FixFileHelper.deleteDirectory(tempDirectory);
            } catch (Exception e) {
                e.printStackTrace();
                FixReceiver.broadcast(this, version, FixReceiver.CODE_SO_ERROR, "修复类失败");
                FixFileHelper.clearDirectory(FixFileHelper.getPrivateDirectory(this));
            }
            FixLogger.log("修复so成功");
        }
        FixReceiver.broadcast(this, version, FixReceiver.CODE_SUCCESS, "修复成功");
    }

    /**
     * 获取补丁名字 http://xxx/xx.apk => xx.apk
     * @param url
     * @return
     */
    private String getPatchName(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return "patch.apk";
        }
        return url.substring(index + 1);
    }

    /**
     * 启动修复服务
     * @param context
     * @param version
     * @param url
     */
    public static void start(Context context, int version, String url) {
        Intent intent = new Intent(context, FixService.class);
        intent.putExtra(FIELD_VERSION, version);
        intent.putExtra(FIELD_URL, url);
        context.startService(intent);
    }

}

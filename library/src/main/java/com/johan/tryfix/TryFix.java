package com.johan.tryfix;

import android.content.Context;

import com.johan.tryfix.helper.FixFileHelper;
import com.johan.tryfix.helper.FixLogger;
import com.johan.tryfix.helper.VersionManager;
import com.johan.tryfix.loader.FixClassLoader;
import com.johan.tryfix.loader.FixResourceLoader;
import com.johan.tryfix.loader.FixSOLoader;
import com.johan.tryfix.service.FixService;

/**
 * Created by johan on 2019/3/15.
 */

public class TryFix {

    /**
     * 修复
     * @param context
     * @param version
     * @param url
     */
    public static void fix(Context context, int version, String url) {
        int lastVersion = getVersion(context);
        if (lastVersion >= version) return;
        FixService.start(context, version, url);
    }

    /**
     * 打补丁
     * @param context
     */
    public static void patch(Context context) {
        // 打类补丁
        if (FixFileHelper.exist(FixFileHelper.getDexPath(context))) {
            FixClassLoader.inject(context.getClassLoader(), FixFileHelper.getDexPath(context), FixFileHelper.getODexFileDirectory(context));
            FixLogger.log("打类补丁完成");
        }
        // 打资源补丁
        if (FixFileHelper.exist(FixFileHelper.getResourcePath(context))) {
            FixResourceLoader.replace(context, FixFileHelper.getResourcePath(context));
            FixLogger.log("打资源补丁完成");
        }
        // 打库补丁
        if (FixFileHelper.exist(FixFileHelper.getSoPath(context))) {
            FixSOLoader.add(context.getClassLoader(), FixFileHelper.getSoPath(context));
            FixLogger.log("打库补丁完成");
        }
    }

    /**
     * 获取修复版本号
     * @param context
     * @return
     */
    public static int getVersion(Context context) {
        return VersionManager.getVersion(context);
    }

    /**
     * 开启调试模式
     */
    public static void debug() {
        FixLogger.debug();
    }

}

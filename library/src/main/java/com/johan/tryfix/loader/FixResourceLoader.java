package com.johan.tryfix.loader;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * Created by johan on 2018/6/11.
 */

public class FixResourceLoader {

    /**
     * 反射获取 ActivityThread
     * @param context
     * @param activityThread
     * @return
     */
    @Nullable
    private static Object getActivityThread(@Nullable Context context, @Nullable Class<?> activityThread) {
        try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method currentActivityThreadMethod = activityThread.getMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApkField = context.getClass().getField("mLoadedApk");
                mLoadedApkField.setAccessible(true);
                Object mLoadedApk = mLoadedApkField.get(context);
                Field mActivityThreadField = mLoadedApk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(mLoadedApk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }

    /**
     * 代替资源
     * @param context
     * @param resourcePath
     */
    public static void replace(Context context, String resourcePath) {
        try {
            // Create a new AssetManager instance and point it to the resources installed under
            // /sdcard
            AssetManager newAssetManager = AssetManager.class.getConstructor().newInstance();
            Method addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            if (((Integer) addAssetPathMethod.invoke(newAssetManager, resourcePath)) == 0) {
                throw new IllegalStateException("Could not create new AssetManager");
            }
            // Kitkat needs this method call, Lollipop doesn't. However, it doesn't seem to cause any harm
            // in L, so we do it unconditionally.
            Method ensureStringBlocksMethod = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocksMethod.setAccessible(true);
            ensureStringBlocksMethod.invoke(newAssetManager);
            // Iterate over all known Resources objects
            Collection<WeakReference<Resources>> references;
            if (SDK_INT >= KITKAT) {
                // Find the singleton instance of ResourcesManager
                Class<?> resourcesManagerClass = Class.forName("android.app.ResourcesManager");
                Method getInstanceMethod = resourcesManagerClass.getDeclaredMethod("getInstance");
                getInstanceMethod.setAccessible(true);
                Object resourcesManager = getInstanceMethod.invoke(null);
                try {
                    Field mActiveResourcesField = resourcesManagerClass.getDeclaredField("mActiveResources");
                    mActiveResourcesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    ArrayMap<?, WeakReference<Resources>> arrayMap =
                            (ArrayMap<?, WeakReference<Resources>>) mActiveResourcesField.get(resourcesManager);
                    references = arrayMap.values();
                } catch (NoSuchFieldException ignore) {
                    Field mResourceReferencesField = resourcesManagerClass.getDeclaredField("mResourceReferences");
                    mResourceReferencesField.setAccessible(true);
                    //noinspection unchecked
                    references = (Collection<WeakReference<Resources>>) mResourceReferencesField.get(resourcesManager);
                }
            } else {
                Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Field mActiveResourcesField = activityThreadClass.getDeclaredField("mActiveResources");
                mActiveResourcesField.setAccessible(true);
                Object thread = getActivityThread(context, activityThreadClass);
                @SuppressWarnings("unchecked")
                HashMap<?, WeakReference<Resources>> map =
                        (HashMap<?, WeakReference<Resources>>) mActiveResourcesField.get(thread);
                references = map.values();
            }
            for (WeakReference<Resources> weakReference : references) {
                Resources resources = weakReference.get();
                if (resources != null) {
                    // Set the AssetManager of the Resources instance to our brand new one
                    try {
                        Field mAssetsField = Resources.class.getDeclaredField("mAssets");
                        mAssetsField.setAccessible(true);
                        mAssetsField.set(resources, newAssetManager);
                    } catch (Throwable ignore) {
                        Field mResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                        mResourcesImplField.setAccessible(true);
                        Object resourceImpl = mResourcesImplField.get(resources);
                        Field mAssetsField = resourceImpl.getClass().getDeclaredField("mAssets");
                        mAssetsField.setAccessible(true);
                        mAssetsField.set(resourceImpl, newAssetManager);
                    }
                    context.getResources().updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
                }
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

}

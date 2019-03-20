package com.johan.tryfix.loader;

import java.io.File;
import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;

/**
 * Created by johan on 2018/6/8.
 */

public class FixClassLoader extends BaseDexClassLoader {

    public FixClassLoader(String dexPath, File optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    /**
     * 利用反射设置ClassLoader的父ClassLoader
     * @param classLoader
     * @param newParent
     */
    private static void setParent(ClassLoader classLoader, ClassLoader newParent) {
        try {
            Field parent = ClassLoader.class.getDeclaredField("parent");
            parent.setAccessible(true);
            parent.set(classLoader, newParent);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 利用反射获取ClassLoader的本地库
     * @param classLoader
     * @return
     */
    private static String getNativeLibraryPath(ClassLoader classLoader) {
        try {
            String nativeLibraryPath = (String) classLoader.getClass().getMethod("getLdLibraryPath", new Class[0])
                    .invoke(classLoader, new Object[0]);
            return nativeLibraryPath;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * 注入FixClassLoader
     * 委托方向
     * 原来     : PathClassLoader -> BootClassLoader
     * 注入之后 : PathClassLoader -> FixClassLoader -> BootClassLoader
     * @param classLoader
     * @param dexPath
     * @param optimizedPath
     * 注意 : 在Application的attachBaseContext方法内调用
     */
    public static void inject(ClassLoader classLoader, String dexPath, String optimizedPath) {
        String nativeLibraryPath = getNativeLibraryPath(classLoader);
        FixClassLoader fixClassLoader = new FixClassLoader(dexPath, new File(optimizedPath), nativeLibraryPath, classLoader.getParent());
        setParent(classLoader, fixClassLoader);
    }

}

package com.johan.tryfix.loader;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.DexFile;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.N_MR1;

/**
 * Created by johan on 2019/3/14.
 */

public class FixSOLoader {

    /**
     * 添加修复SO文件夹
     * CPU类型
        armeabi（一般都会有）
        armeabi-v7a（大部分机型）
        arm64-v8a （高端机型）
        x86
        x86_64
        mips
        mips64
     * @param classLoader 类加载器
     * @param soPath 修复so所在私有文件夹
     * 这里特别要注意一下 so不能是放在SD卡中 因为没有权限加载SD卡的so文件 会报
     * java.lang.UnsatisfiedLinkError: dlopen failed: couldn't map "/storage/emulated/0/xxx.so" segment 0: Operation not permitted
     * 所以得把SD卡的so文件copy到私有目录才能正常加载
     * 获取私有目录 see {@link com.johan.tryfix.helper.FixFileHelper#getPrivateDirectory(Context)}
     * so文件夹结构 :
     * - soPath
     *   -- armeabi
     *   -- armeabi-v7a
     *   -- arm64-v8a
     *   ......
     */
    public static void add(ClassLoader classLoader, String soPath) {
        String cpu = Build.CPU_ABI;
        File soPathFile = new File(soPath, cpu);
        if (!soPathFile.exists()) {
            soPathFile = new File(soPath, "armeabi");
        }
        if (!soPathFile.exists()) return;
        // PathClassLoader extends BaseDexClassLoader
        // BaseDexClassLoader才有pathList字段
        Class loaderClass = classLoader.getClass().getSuperclass();
        try {
            // 反射获取PathClassLoader(父类BaseDexClassLoader)的pathList(DexPathList类型)字段
            Field pathListField = getField(loaderClass, "pathList");
            Class pathListClass = pathListField.getType();
            Object pathList = pathListField.get(classLoader);
            if (pathList == null) return;
            // 8.0 - 9.0
            if (SDK_INT > N_MR1) {
                // 获取DexPathList的nativeLibraryPathElements(NativeLibraryElement[])字段
                Field nativeLibraryField = getField(pathListClass, "nativeLibraryPathElements");
                Object nativeLibrary = nativeLibraryField.get(pathList);
                if (nativeLibrary == null) return;
                // 新建NativeLibraryElement
                Class elementClass = Class.forName("dalvik.system.DexPathList$NativeLibraryElement");
                Constructor constructor = elementClass.getDeclaredConstructor(File.class);
                Object element = constructor.newInstance(soPathFile);
                // 合成新的nativeLibraryDirectories
                Object newNativeLibrary = combineArray(nativeLibrary, new Object[]{element});
                // 重新赋值
                nativeLibraryField.set(pathList, newNativeLibrary);
            }
            // 6.0 - 7.1
            else if (SDK_INT > LOLLIPOP_MR1 && SDK_INT <= N_MR1) {
                // 获取DexPathList的nativeLibraryPathElements(Element[])字段
                Field nativeLibraryField = getField(pathListClass, "nativeLibraryPathElements");
                Object nativeLibrary = nativeLibraryField.get(pathList);
                if (nativeLibrary == null) return;
                // 新建Element
                Class elementClass = Class.forName("dalvik.system.DexPathList$Element");
                Constructor constructor = elementClass.getDeclaredConstructor(File.class, boolean.class, File.class, DexFile.class);
                Object element = constructor.newInstance(soPathFile, true, null, null);
                // 合成新的nativeLibraryDirectories
                Object newNativeLibrary = combineArray(nativeLibrary, new Object[]{element});
                // 重新赋值
                nativeLibraryField.set(pathList, newNativeLibrary);
            }
            // 6.0 以下
            else {
                // 获取DexPathList的nativeLibraryDirectories(File[])字段
                Field nativeLibraryField = getField(pathListClass, "nativeLibraryDirectories");
                Object nativeLibrary = nativeLibraryField.get(pathList);
                if (nativeLibrary == null) return;
                // 合成新的nativeLibraryDirectories
                Object newNativeLibrary = combineArray(nativeLibrary, new Object[]{soPathFile});
                // 重新赋值
                nativeLibraryField.set(pathList, newNativeLibrary);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取字段
     * @param clazz
     * @param fieldName
     * @return
     * @throws NoSuchFieldException
     */
    private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * 组合数组
     * @param array1
     * @param array2
     * @return
     */
    private static Object combineArray(Object array1, Object array2) {
        Class<?> clazz = array1.getClass().getComponentType();
        int length1 = Array.getLength(array1);
        int length2 = Array.getLength(array2);
        int length = length1 + length2;
        Object array = Array.newInstance(clazz, length);
        System.arraycopy(array2, 0, array, 0, length2);
        System.arraycopy(array1, 0, array, length2, length1);
        return array;
    }

}

package com.johan.tryfix.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by johan on 2019/3/15.
 */

public class FixFileHelper {

    public static final String FIX_DIRECTORY = "fix";
    public static final String TEMP_DIRECTORY = "temp";
    public static final String DEX_FILE = "classes.dex";
    public static final String O_DEX_FILE_DIRECTORY = "odex";
    public static final String RES_DIRECTORY = "res";
    public static final String ARSC_DIRECTORY = "resources.arsc";
    public static final String APK_DIRECTORY = "apk";
    public static final String RESOURCE_FILE = "resource.apk";
    public static final String SO_DIRECTORY = "lib";

    /**
     * 获取私有修复路径
     * @param context
     * @return
     */
    public static String getPrivateDirectory(Context context) {
        String privateDirectoryPath = context.getDir(FIX_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath();
        createDirectory(privateDirectoryPath);
        return privateDirectoryPath;
    }

    /**
     * 获取修复临时路径
     * @param context
     * @return
     */
    public static String getTempDirectory(Context context) {
        String tempDirectoryPath = context.getDir(FIX_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath() + File.separator + TEMP_DIRECTORY;
        createDirectory(tempDirectoryPath);
        return tempDirectoryPath;
    }

    /**
     * 获取临时classes.dex路径
     * @param context
     * @return
     */
    public static String getTempDexPath(Context context) {
        return getTempDirectory(context) + File.separator + DEX_FILE;
    }

    /**
     * 获取修复classes.dex路径
     * @param context
     * @return
     */
    public static String getDexPath(Context context) {
        return getPrivateDirectory(context) + File.separator + DEX_FILE;
    }

    /**
     * 获取ODex的路径
     * @param context
     * @return
     */
    public static String getODexFileDirectory(Context context) {
        String oDexDirectoryPath = context.getDir(FIX_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath() + File.separator + O_DEX_FILE_DIRECTORY;
        createDirectory(oDexDirectoryPath);
        return oDexDirectoryPath;
    }

    /**
     * 获取临时res的路径
     * @param context
     * @return
     */
    public static String getTempResPath(Context context) {
        return getTempDirectory(context) + File.separator + RES_DIRECTORY;
    }

    /**
     * 获取临时resources.arsc的路径
     * @param context
     * @return
     */
    public static String getTempArscPath(Context context) {
        return getTempDirectory(context) + File.separator + ARSC_DIRECTORY;
    }

    /**
     * 获取源Apk的路径
     * @param context
     * @return
     */
    public static String getSourceApkPath(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        String apkPath = applicationInfo.sourceDir;
        return apkPath;
    }

    /**
     * 获取源Apk解压路径
     * @param context
     * @return
     */
    public static String getSourceApkDirectory(Context context) {
        String apkDirectorPath = context.getDir(FIX_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath() + File.separator + APK_DIRECTORY;
        createDirectory(apkDirectorPath);
        return apkDirectorPath;
    }

    /**
     * 获取修复资源路径
     * @param context
     * @return
     */
    public static String getResourcePath(Context context) {
        return getPrivateDirectory(context) + File.separator + RESOURCE_FILE;
    }

    /**
     * 获取临时so路径
     * @param context
     * @return
     */
    public static String getTempSoDirectory(Context context) {
        return getTempDirectory(context) + File.separator + SO_DIRECTORY;
    }

    /**
     * 获取修复so路径
     * @param context
     * @return
     */
    public static String getSoPath(Context context) {
        return getPrivateDirectory(context) + File.separator + SO_DIRECTORY;
    }

    /**
     * 复制文件夹
     * @param source
     * @param target
     */
    public static void copyDirectory(String source, String target) {
        File directory = new File(source);
        if (!directory.exists() || !directory.isDirectory()) return;
        createDirectory(target);
        for (File file : directory.listFiles()) {
            String childSource = source + File.separator + file.getName();
            String childTarget = target + File.separator + file.getName();
            if (file.isDirectory()) {
                createDirectory(childTarget);
                copyDirectory(childSource, childTarget);
            } else {
                copyFile(childSource, childTarget);
            }
        }
    }

    /**
     * 复制文件
     * @param source
     * @param target
     */
    public static void copyFile(String source, String target) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建文件夹
     * @param directoryPath
     */
    public static void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists()) return;
        directory.mkdirs();
    }

    /**
     * 清空文件夹
     * @param directoryPath
     */
    public static void clearDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists()) return;
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                clearDirectory(file.getAbsolutePath());
            } else {
                file.delete();
            }
        }
    }

    /**
     * 删除文件夹
     * @param directoryPath
     */
    public static void deleteDirectory(String directoryPath) {
        clearDirectory(directoryPath);
        deleteFile(directoryPath);
    }

    /**
     * 删除文件
     * @param filePath
     */
    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;
        file.delete();
    }

    /**
     * 解压
     * @param sourceZip
     * @param targetDirectory
     * @throws Exception
     */
    public static void unzip(String sourceZip, String targetDirectory) throws Exception{
        Project project = new Project();
        Expand expand = new Expand();
        expand.setProject(project);
        expand.setSrc(new File(sourceZip));
        expand.setOverwrite(true);
        expand.setDest(new File(targetDirectory));
        expand.setEncoding("gbk");
        expand.execute();
    }

    /**
     * 压缩
     * @param sourceDirectory
     * @param targetZip
     */
    public static void zip(String sourceDirectory, String targetZip) {
        Project project = new Project();
        Zip zip = new Zip();
        zip.setProject(project);
        zip.setDestFile(new File(targetZip));
        FileSet fileSet = new FileSet();
        fileSet.setProject(project);
        File file = new File(sourceDirectory);
        if (file.isDirectory()) {
            fileSet.setDir(file);
        } else {
            fileSet.setFile(file);
        }
        zip.addFileset(fileSet);
        zip.setEncoding("gbk");
        zip.execute();
    }

    /**
     * 代替
     * @param sourcePath
     * @param targetPath
     * @throws IOException
     */
    public static void replace(String sourcePath, String targetPath) throws Exception {
        File sourceDirectory = new File(sourcePath);
        File[] sourceFiles = sourceDirectory.listFiles();
        for (File sourceFile : sourceFiles) {
            File targetFile = new File(targetPath + "/" + sourceFile.getName());
            if (sourceFile.isFile()) {
                copyFile(sourceFile.getPath(), targetFile.getPath());
            }
            if (sourceFile.isDirectory()) {
                replace(sourceFile.getPath(), targetFile.getPath());
            }
        }
    }

    /**
     * 判断文件是否存在
     * @param path
     * @return
     */
    public static boolean exist(String path) {
        File file = new File(path);
        return file.exists();
    }

}

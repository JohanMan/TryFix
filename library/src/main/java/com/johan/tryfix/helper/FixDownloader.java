package com.johan.tryfix.helper;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by johan on 2018/8/27.
 */

public class FixDownloader {

    /**
     * 下载
     * 同步方法
     * @param urlPath
     * @param filePath
     * @return
     */
    public static boolean download(String urlPath, String filePath) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(filePath);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                FixLogger.log("下载成功");
                return true;
            }
            FixLogger.log("下载失败 : 响应码(" + connection.getResponseCode() + ")");
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            FixLogger.log("下载错误 : " + e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            FixLogger.log("下载错误 : " + e.getLocalizedMessage());
            return false;
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
    }

    /**
     * 关闭Stream
     * @param closeable
     */
    private static void closeStream(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

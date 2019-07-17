package com.example.tommy.camerademo;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final String TAG = "wanghuan";

    private static final String FILE_DIR = "MyCamera";

    /**
     * 设置存储路径
     *
     * @return
     */
    public static String getFilePath(Context context, Media media) {
        File storageDir = getOwnCacheDirectory(context, FILE_DIR + "/" + media.getType());
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + media.getSuffix();
        return storageDir.getPath() + "/" + filename;
    }

    private static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        //判断SD卡正常挂载并且拥有根限的时候创建文件
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    /**
     * 检查是否有权限
     *
     * @param context
     * @return
     */
    private static boolean hasExternalStoragePermission(Context context) {
        int permission = context.checkCallingOrSelfPermission("android.permission" +
                ".WRITE_EXTERNAL_STORAGE");
        return permission == 0;
    }

    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}

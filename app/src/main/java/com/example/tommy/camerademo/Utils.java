package com.example.tommy.camerademo;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static final String TAG = "CameraDemo";

    public static final String MEDIA_INX = "media_inx";
    private static final String FILE_DIR = "MyCamera";

    /**
     * 隐藏导航栏并且全屏显示
     */
    public static void hideNavigationBar(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //设置屏幕常亮
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 设置存储路径
     */
    public static String getFilePath(Context context, Media media) {
        File storageDir = getOwnCacheDirectory(context, FILE_DIR + "/" + media.getType());
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + media.getSuffix();
        return storageDir.getPath() + "/" + filename;
    }

    private static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        //判断SD卡正常挂载并且有权限的时候创建文件
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int permission = context.checkCallingOrSelfPermission("android.permission" +
                ".WRITE_EXTERNAL_STORAGE");
        return permission == 0;
    }

    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static void savePhoto(Context context, byte[] data) {
        FileOutputStream fos = null;
        String filePath = getFilePath(context, Media.PHOTO);
        try {
            fos = new FileOutputStream(filePath);
            fos.write(data);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

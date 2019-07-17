package com.example.tommy.camerademo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static com.example.tommy.camerademo.Utils.TAG;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera(MainActivity.this, Media.PHOTO);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera(MainActivity.this, Media.VIDEO);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TakePhotoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityPermissionsDispatcher.applyPermissionsWithPermissionCheck(this);
    }

    /**
     * https://blog.csdn.net/u010356768/article/details/70808162
     *
     * @param activity
     */
    private void openCamera(Activity activity, Media media) {
        // 激活相机
        Intent intent = new Intent(media.getAction());
        // 判断存储卡是否可以用，可用进行存储
        if (Utils.hasSdcard()) {
            // Android7.0之前的方式，从文件中创建uri
            /*
            mediaUri = Uri.fromFile(new File(Utils.getFilePath(this, media)));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
            */

            // 兼容Android7.0，使用共享文件的形式
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.MediaColumns.DATA, Utils.getFilePath(this, media));
            // 检查是否有存储权限，以免崩溃
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 申请WRITE_EXTERNAL_STORAGE权限
                Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri mediaUri = activity.getContentResolver().insert(media.getUrl(), contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);
        }
        activity.startActivity(intent);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void applyPermissions() {
        Log.i(TAG, "apply permissions");
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void denied() {
        Log.i(TAG, "permission denied");
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode,
                grantResults);
    }
}

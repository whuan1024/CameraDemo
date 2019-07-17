package com.example.tommy.camerademo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * https://github.com/Liangzhuhua/MyCamera
 */
public class TakePhotoActivity extends AppCompatActivity {

    private static final String TAG = "wanghuan";

    private static SurfaceView surfaceView;               //图像实时窗口
    private Button btn_takePhoto;                         //拍照按钮
    private SurfaceHolder surfaceHolder;                  //定义访问surfaceView的接口

    private static final int CAMERA_NOTEXIST = -1;        //无摄像头标记
    private static final int FRONT = 1;                   //前置摄像头标记
    private static final int BACK = 2;                    //后置摄像头标记
    private int currentCameraType = CAMERA_NOTEXIST;      //当前打开摄像头标记
    private int currentCameraIndex = -1;                  //当前摄像头下标
    private boolean mPreviewRunning = false;              //预览是否启动

    private Camera mCamera = null;                        //Camera对象

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //竖屏
        hideNavigationBar(); //隐藏导航栏并且全屏显示
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //设置屏幕常亮
        initView();
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 初始化
     */
    private void initView() {
        surfaceView = findViewById(R.id.camera_surfaceView);
        btn_takePhoto = findViewById(R.id.btn_takePhoto);

        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(new CameraSurfaceCallBack());
        btn_takePhoto.setOnClickListener(new BtnTakePhotoListener());
    }

    /**
     * 查找摄像头
     *
     * @param camera_facing 按要求查找，镜头是前还是后
     * @return -1表示找不到
     */
    private int findBackOrFrontCamera(int camera_facing) {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == camera_facing) {
                return camIdx;
            }
        }
        return -1;
    }

    /**
     * 按照type的类型打开相应的摄像头
     *
     * @param type 标志当前打开前还是后的摄像头
     * @return 返回当前打开摄像机的对象
     */
    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();

        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            currentCameraIndex = frontIndex;
            return Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            currentCameraIndex = backIndex;
            return Camera.open(backIndex);
        }
        return null;
    }

    /**
     * 初始化摄像头
     *
     * @param holder
     */
    private void initCamera(SurfaceHolder holder) {
        Log.i(TAG, "initCamera");
        if (mPreviewRunning)
            mCamera.stopPreview();

        Camera.Parameters parameters;
        try {
            //获取预览的各种分辨率
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //这里我设为1440*2560的尺寸
        parameters.setPreviewSize(1440, 2560);
        // 设置照片格式
        parameters.setPictureFormat(PixelFormat.JPEG);
        //设置图片预览的格式
        parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        setCameraDisplayOrientation(this, currentCameraIndex, mCamera);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    /**
     * 设置旋转角度
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Log.i(TAG,"rotation:"+rotation);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 实现拍照功能
     */
    public void takePhoto() {
        Camera.Parameters parameters;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //获取摄像头支持的各种分辨率,因为摄像头数组不确定是按降序还是升序，这里的逻辑有时不是很好找得到相应的尺寸
        //可先确定是按升还是降序排列，再进对对比吧，我这里拢统地找了个，是个不精确的...
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        int size = 0;
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).width >= 480) {
                //完美匹配
                size = i;
                break;
            } else {
                //找不到就找个最接近的吧
                size = i;
            }
        }
        //设置照片分辨率，注意要在摄像头支持的范围内选择
        parameters.setPictureSize(list.get(size).width, list.get(size).height);
        //设置照相机参数
        mCamera.setParameters(parameters);

        //使用takePicture()方法完成拍照
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            //自动聚焦完成后拍照
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success && camera != null) {
                    mCamera.takePicture(new ShutterCallback(), null, new Camera.PictureCallback() {
                        //拍照回调接口
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            savePhoto(data);
                            //停止预览
                            mCamera.stopPreview();
                            //重启预览
                            mCamera.startPreview();
                        }
                    });
                }
            }
        });
    }

    /**
     * 快门回调接口，如果不想拍照声音，直接将new ShutterCallback()修改为null即可
     */
    private class ShutterCallback implements Camera.ShutterCallback {
        @Override
        public void onShutter() {
            MediaPlayer mPlayer = new MediaPlayer();
            mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.shutter);
            try {
                mPlayer.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.start();
        }
    }

    /**
     * 将拍照保存下来
     *
     * @param data
     */
    public void savePhoto(byte[] data) {
        FileOutputStream fos;
        //保存路径+图片名字
        String imagePath = Utils.getFilePath(this, Media.PHOTO);
        try {
            fos = new FileOutputStream(imagePath);
            fos.write(data);
            //清空缓冲区数据
            fos.flush();
            //关闭
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Toast.makeText(this, "拍照成功!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 拍照按钮事件回调
     */
    public class BtnTakePhotoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.i(TAG, "拍照按钮事件回调");
            takePhoto();
        }
    }

    /**
     * surfaceView实例回调
     */
    public class CameraSurfaceCallBack implements SurfaceHolder.Callback {

       /* public CameraSurfaceCallBack(Context context) {
            super(context);
        }*/

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "------surfaceCreated------");
            try {
                //这里我优先找前置摄像头,找不到再找后面的
                int cameraIndex = findBackOrFrontCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                if (cameraIndex == -1) {
                    cameraIndex = findBackOrFrontCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (cameraIndex == -1) {
                        Log.i(TAG, "No Camera!");
                        currentCameraType = CAMERA_NOTEXIST;
                        currentCameraIndex = -1;
                        return;
                    } else {
                        currentCameraType = BACK;
                    }
                } else {
                    currentCameraType = FRONT;
                }

                //找到想要的摄像头后，就打开
                if (mCamera == null) {
                    mCamera = openCamera(currentCameraType);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "------surfaceChanged------");
            initCamera(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
package com.example.tommy.camerademo;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.tommy.camerademo.Utils.TAG;

/**
 * 参考 https://blog.csdn.net/toyauko/article/details/82220933
 */
public class TakePhotoActivity extends AppCompatActivity {

    private Camera mCamera; //Camera对象

    private static final int FRONT = 1;      //前置摄像头标记
    private static final int BACK = 2;       //后置摄像头标记
    private int mCurrentCameraType = -1;     //当前打开摄像头标记
    private int mCurrentCameraIndex = -1;    //当前摄像头下标
    private boolean mPreviewRunning = false; //预览是否启动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.hideNavigationBar(this);
        setContentView(R.layout.activity_take_camera);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceCallBack());

        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    /**
     * 查找摄像头
     *
     * @param cameraFacing 按要求查找，是前置还是后置
     * @return -1表示找不到
     */
    private int findBackOrFrontCamera(int cameraFacing) {
        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                return camIdx;
            }
        }
        return -1;
    }

    /**
     * 按照type的类型打开相应的摄像头
     *
     * @param type 标志当前打开前置摄像头还是后置摄像头
     * @return 返回当前打开摄像头的对象
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

        mCurrentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            mCurrentCameraIndex = frontIndex;
            return Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            mCurrentCameraIndex = backIndex;
            return Camera.open(backIndex);
        }
        return null;
    }

    /**
     * 初始化摄像头
     */
    private void initCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        //此处全部使用默认参数，如需自定义，则取消以下注释，自行定义参数
        /*
        //设置图片预览的格式
        parameters.setPreviewFormat(ImageFormat.NV21);
        //设置图片预览的尺寸
        parameters.setPreviewSize(1920, 1080);
        //设置照片格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        //获取摄像头支持的各种分辨率，该集合是按照降序排列的
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        //设置照片尺寸，这里取最大分辨率
        parameters.setPictureSize(list.get(0).width, list.get(0).height);
        */
        mCamera.setParameters(parameters);
    }

    /**
     * 开始预览界面
     */
    private void startPreview(SurfaceHolder holder) {
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }
        setCameraDisplayOrientation(this, mCurrentCameraIndex, mCamera);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    /**
     * 设置旋转角度
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degree + 360) % 360;
        }
        Log.i(TAG, "rotation:" + rotation + " degree:" + degree + " result:" + result);
        camera.setDisplayOrientation(result);
    }

    /**
     * 实现拍照功能
     */
    private void takePhoto() {
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
                            Utils.savePhoto(TakePhotoActivity.this, data);
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
            MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.shutter);
            try {
                player.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.start();
        }
    }

    /**
     * 释放摄像头资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * surfaceView实例回调
     */
    private class SurfaceCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "------surfaceCreated------");
            try {
                //优先查找前置摄像头，找不到再找后置摄像头
                int cameraIndex = findBackOrFrontCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                if (cameraIndex == -1) {
                    cameraIndex = findBackOrFrontCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (cameraIndex == -1) {
                        Log.e(TAG, "No Camera!");
                        mCurrentCameraType = -1;
                        mCurrentCameraIndex = -1;
                        return;
                    } else {
                        mCurrentCameraType = BACK;
                    }
                } else {
                    mCurrentCameraType = FRONT;
                }

                //打开摄像头
                if (mCamera == null) {
                    mCamera = openCamera(mCurrentCameraType);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "------surfaceChanged------");
            initCamera();
            startPreview(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "------surfaceDestroyed------");
            releaseCamera();
        }
    }
}

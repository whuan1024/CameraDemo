package com.example.tommy.camerademo;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.tommy.camerademo.Utils.TAG;

/**
 * 参考 https://my.oschina.net/xiaoaimiao/blog/1574717
 *     https://github.com/Yaphetwyf/Camera
 */
public class TakeVideoActivity extends AppCompatActivity {

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Chronometer mTimer;
    private Button mTakeVideo;

    /** 标记当前是否正在录制 */
    private boolean mIsRecording = false;

    /** The settings used for video recording */
    private CamcorderProfile mCamcorderProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.hideNavigationBar(this);
        setContentView(R.layout.activity_take_video);
        initView();
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surface_view);
        mTimer = findViewById(R.id.chronometer);
        mTakeVideo = findViewById(R.id.take_video);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceCallBack());

        mTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo();
            }
        });
    }

    private void takeVideo() {
        if (mIsRecording) {
            Log.i(TAG, "Stop recording.");
            stopRecording();
            updateUI(false);
        } else {
            if (startRecording()) {
                Log.i(TAG, "Start recording.");
                updateUI(true);
            }
        }
    }

    private void updateUI(boolean isRecording) {
        mTimer.setBase(SystemClock.elapsedRealtime());
        if (isRecording) {
            mTimer.start();
            mTakeVideo.setBackgroundResource(R.drawable.btn_shutter_video_recording);
        } else {
            mTimer.stop();
            mTakeVideo.setBackgroundResource(R.drawable.btn_shutter_video_default);
        }
    }

    private void initCamera() {
        Camera.Parameters parameters = mCamera.getParameters();
        //此处全部使用默认参数，如需自定义，则取消以下注释，自行定义参数
        /*
        //设置对焦模式
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //设置图像预览的格式
        parameters.setPreviewFormat(ImageFormat.NV21);
        //设置图像预览的尺寸
        parameters.setPreviewSize(1920, 1080);
        //获取摄像头支持的各种分辨率，该集合是按照降序排列的
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        //设置图像尺寸，这里取最大分辨率
        parameters.setPictureSize(list.get(0).width, list.get(0).height);
        */
        mCamera.setParameters(parameters);
    }

    private void startPreview(SurfaceHolder holder) {
        mCamera.setDisplayOrientation(90); //解决竖屏时摄像头自动旋转90度的问题
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        //默认使用后置摄像头，拍摄1080p视频
        mCamcorderProfile = CamcorderProfile.get(0, CamcorderProfile.QUALITY_1080P);
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(mCamcorderProfile);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setOutputFile(Utils.getFilePath(this, Media.VIDEO));
        try {
            mMediaRecorder.prepare();
            return true;
        } catch (IOException e) {
            releaseMediaRecorder();
            e.printStackTrace();
        }
        return false;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private boolean startRecording() {
        if (prepareMediaRecorder()) {
            mMediaRecorder.start();
            mIsRecording = true;
        } else {
            releaseMediaRecorder();
            mIsRecording = false;
        }
        return mIsRecording;
    }

    private void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
        mIsRecording = false;
    }

    private class SurfaceCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "------surfaceCreated------");
            try {
                if (mCamera == null) {
                    mCamera = Camera.open(0); //默认打开后置摄像头
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
            if (mIsRecording) {
                Log.i(TAG, "Stop recording after surface destroyed.");
                stopRecording();
                updateUI(false);
            }
            releaseCamera();
            releaseMediaRecorder();
        }
    }
}

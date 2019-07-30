package com.example.tommy.camerademo;

import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.tommy.camerademo.Utils.TAG;

public class CameraKitActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraView mCameraView;
    private Chronometer mTimer;
    private Button mTakeVideo;
    private Button mTakePhoto;

    /** 标记当前是否正在录制 */
    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.hideNavigationBar(this);
        setContentView(R.layout.activity_camera_kit);
        initView();
    }

    private void initView() {
        mCameraView = findViewById(R.id.camera_view);
        mTimer = findViewById(R.id.chronometer);
        mTakeVideo = findViewById(R.id.take_video);
        mTakePhoto = findViewById(R.id.take_photo);
        int mediaInx = getIntent().getIntExtra(Utils.MEDIA_INX, 0);
        if (mediaInx == Media.PHOTO.getIndex()) {
            mTimer.setVisibility(View.GONE);
            mTakeVideo.setVisibility(View.GONE);
            mTakePhoto.setVisibility(View.VISIBLE);
            mCameraView.setFacing(CameraKit.Constants.FACING_FRONT); //使用前置摄像头拍照
        } else if (mediaInx == Media.VIDEO.getIndex()) {
            mTimer.setVisibility(View.VISIBLE);
            mTakeVideo.setVisibility(View.VISIBLE);
            mTakePhoto.setVisibility(View.GONE);
            mCameraView.setFacing(CameraKit.Constants.FACING_BACK); //使用后置摄像头录像
            mCameraView.setVideoQuality(CameraKit.Constants.VIDEO_QUALITY_1080P); //录制1080p视频
        }
        mTakeVideo.setOnClickListener(this);
        mTakePhoto.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        if (mIsRecording) {
            Log.i(TAG, "Stop recording after activity paused.");
            mCameraView.stopVideo();
            mIsRecording = false;
            updateUI(false);
        }
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo:
                takePhoto();
                break;
            case R.id.take_video:
                takeVideo();
                break;
        }
    }

    private void takePhoto() {
        new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
        mCameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage cameraKitImage) {
                Utils.savePhoto(CameraKitActivity.this, cameraKitImage.getJpeg());
            }
        });
    }

    private void takeVideo() {
        if (mIsRecording) {
            Log.i(TAG, "Stop recording with CameraKit.");
            mCameraView.stopVideo();
            mIsRecording = false;
        } else {
            Log.i(TAG, "Start recording with CameraKit.");
            mCameraView.captureVideo(new File(Utils.getFilePath(this, Media.VIDEO)));
            mIsRecording = true;
        }
        updateUI(mIsRecording);
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
}

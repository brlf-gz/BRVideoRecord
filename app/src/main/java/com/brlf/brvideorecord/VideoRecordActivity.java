package com.brlf.brvideorecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brlf.videorecord.camerafragment.CameraFragment;
import com.brlf.videorecord.camerafragment.CameraFragmentApi;
import com.brlf.videorecord.camerafragment.configuration.Configuration;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentResultAdapter;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentResultListener;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentStateListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoRecordActivity extends FragmentActivity {

    private static final String TAG = "VideoRecordActivity";
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private static final int REQUEST_CODE_LOCAL = 930;
    public static final int RESULT_ERROR_CODE = 304;

    private CameraFragment mCameraFragment;

    private Button mCloseBtn;
    private Button mFlashBtn;
    private Button mSwitchBtn;
    private Button mTakeBtn;
    private Button mLocalBtn;
    private TextView mTimeText;

    public static final String FRAGMENT_TAG = "camera";

    private boolean mIsFlash = false;
    private boolean mIsBackCamera = true;
    private boolean mIsRecording = false;
    private boolean mIsFinish = false;

    //保存文件夹
    private String mFolderPath = Environment.getExternalStorageDirectory().getPath() + "/BRVideoRecord/";
    private String mFileName;
    private int duration = 0; //计时
    private String durationStr;
    private int degress = 0;
    private int length = 0;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mIsRecording) {
                duration++;
                durationStr = String.format("%02d", duration / 60)
                        + ":"
                        + String.format("%02d", duration % 60);
                mTimeText.setText(durationStr);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 不显示程序的标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 不显示系统的标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_videorecord);

        initView();
        initPermissions();
    }

    //权限获取
    private void initPermissions() {
        if (Build.VERSION.SDK_INT > 15) {
            final String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CAMERA_PERMISSIONS);
            } else {
                initCameraFragment();
            }
        } else {
            initCameraFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length != 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            initCameraFragment();
        } else {
            setResult(RESULT_ERROR_CODE,
                    new Intent().putExtra("message", "授权失败"));
            onBackPressed();
        }
    }

    private void initView(){
        mCloseBtn = (Button)findViewById(R.id.closeButton);
        mSwitchBtn = (Button)findViewById(R.id.switchButton);
        mFlashBtn = (Button)findViewById(R.id.flashButto);
        mTakeBtn = (Button)findViewById(R.id.takeButton);
        mLocalBtn = (Button)findViewById(R.id.localButton);
        mTimeText = (TextView)findViewById(R.id.timeLabel);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //关闭
                if(mIsRecording) {
                    handler.removeCallbacks(runnable);
                    CaptureVideo();
                }
                Intent it = new Intent();
                setResult(RESULT_ERROR_CODE, it);
                finish();
            }
        });

        mFlashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //闪光灯
                final CameraFragmentApi cameraFragment = getCameraFragment();
                if (cameraFragment != null) {
                    cameraFragment.toggleFlashMode();
                }
            }
        });

        mSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //前后摄像头转换
                final CameraFragmentApi cameraFragment = getCameraFragment();
                if (cameraFragment != null) {
                    cameraFragment.switchCameraTypeFrontBack();
                }
            }
        });

        mTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //拍摄
                CaptureVideo();
            }
        });

        mLocalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //本地
                Intent intent;
                Log.e(TAG, android.os.Build.BRAND);
                if(android.os.Build.BRAND.equals("Meizu")) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else{
                    intent = new Intent(Intent.ACTION_PICK);
                }
                intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
                startActivityForResult(intent, REQUEST_CODE_LOCAL);
            }
        });
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void initCameraFragment(){
        //初始化
        final Configuration.Builder builder = new Configuration.Builder();
        builder.setCamera(Configuration.CAMERA_FACE_REAR)
                .setMinimumVideoDuration(3000) //最小
                .setVideoDuration(10000) //最大
                .setFlashMode(Configuration.FLASH_MODE_OFF)
                .setMediaAction(Configuration.MEDIA_ACTION_VIDEO);

        Bundle bundle = this.getIntent().getExtras();
        if (!TextUtils.isEmpty(bundle.getString("minTime"))) {
            builder.setMinimumVideoDuration(bundle.getInt("minTime"));
        }
        if (!TextUtils.isEmpty(bundle.getString("maxTime"))) {
            builder.setVideoDuration(bundle.getInt("maxTime"));
        }
        if (!TextUtils.isEmpty(bundle.getString("cameraX"))) {
        }
        if (!TextUtils.isEmpty(bundle.getString("cameraY"))) {
        }
        if (!TextUtils.isEmpty(bundle.getString("videoRate"))) {
            int duration = bundle.getInt("maxTime")/1000;
            builder.setVideoFileSize(bundle.getLong("videoRate")*1024*duration);
        }
        mCameraFragment = CameraFragment.newInstance(builder.build());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, mCameraFragment, FRAGMENT_TAG)
                .commitAllowingStateLoss();

        if (mCameraFragment != null) {
            //结果监听
            mCameraFragment.setResultListener(new CameraFragmentResultListener() {
                @Override
                public void onVideoRecorded(final String filePath) {
                    //debugMsg(filePath);
                    //Intent intent = PreviewActivity.newIntentVideo(VideoRecordActivity.this, filePath);
                    //startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //getVideoCover(filePath);
                                    getResult(filePath);
                                    mIsFinish = true;
                                }
                            }, 300
                    );
                }

                @Override
                public void onPhotoTaken(byte[] bytes, String filePath) {
                }
            });

            //相机状态监听
            mCameraFragment.setStateListener(new CameraFragmentStateListener() {
                @Override
                public void onCurrentCameraBack() {
                    Log.d(TAG, "切换后端摄像头");
                    mIsBackCamera = true;
                }

                @Override
                public void onCurrentCameraFront() {
                    debugMsg("切换前端摄像头");
                    mIsBackCamera = false;
                }

                @Override
                public void onFlashAuto() {
                    //debugMsg("自动闪光灯");
                    mFlashBtn.setBackgroundResource(R.drawable.flashlight_auto);
                }

                @Override
                public void onFlashOn() {
                    //debugMsg("开启闪光灯");
                    mFlashBtn.setBackgroundResource(R.drawable.flashlight_on);
                    mIsFlash = true;
                }

                @Override
                public void onFlashOff() {
                    //debugMsg("关闭闪光灯");
                    mFlashBtn.setBackgroundResource(R.drawable.flashlight_off);
                    mIsFlash = false;
                }

                @Override
                public void onCameraSetupForPhoto() {}

                @Override
                public void onCameraSetupForVideo() {}

                @Override
                public void shouldRotateControls(int degrees) {
                    /*ViewCompat.setRotation(mCloseBtn, degrees);
                    ViewCompat.setRotation(mFlashBtn, degrees);
                    ViewCompat.setRotation(mSwitchBtn, degrees);
                    ViewCompat.setRotation(mTimeText, degrees);*/
                    VideoRecordActivity.this.degress = degrees + 90;
                }

                @Override
                public void onRecordStateVideoReadyForRecord() {
                    //debugMsg("===准备录像");
                }

                @Override
                public void onRecordStateVideoInProgress() {
                    //debugMsg("===录像进程中");
                }

                @Override
                public void onRecordStatePhoto() {}

                @Override
                public void onStopVideoRecord() {
                    //debugMsg("视频录制完毕");
                    mIsRecording = false;
                    handler.removeCallbacks(runnable);
                    mTimeText.setText("00:00");
                    length = duration;
                    duration = 0;
                }

                @Override
                public void onStartVideoRecord(File outputFile) {
                    mIsRecording = true;
                    handler.postDelayed(runnable, 1000);
                }
            });

        }
    }

    //拍摄或停止
    public void CaptureVideo() {
        final CameraFragmentApi cameraFragment = getCameraFragment();
        if (!mIsRecording) {
            mFileName = "VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            mIsFinish = false;
        }
        if (cameraFragment != null) {
            cameraFragment.takePhotoOrCaptureVideo(
                    new CameraFragmentResultAdapter() {
                        @Override
                        public void onVideoRecorded(String filePath) {
                            //debugMsg("=====>>视频路径：" + filePath);
                        }

                        @Override
                        public void onPhotoTaken(byte[] bytes, String filePath) {
                        }
                    },
                    mFolderPath,
                    mFileName);
        }
    }

    //获取Fragment
    private CameraFragmentApi getCameraFragment() {
        return (CameraFragmentApi) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    private void debugMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_SHORT).show();
    }

    //视频封面图片
    private String getVideoCover(String path){
        Bitmap frameAtTime = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        if(frameAtTime == null) {
            Log.e("bm", "null");
            return null;
        }
        if (Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) { // 判断是否可以对SDcard进行操作
            String  sdCardDir = mFolderPath + "cover/";
            File dirFile  = new File(sdCardDir);
            if (!dirFile .exists()) {
                dirFile .mkdirs();
            }
            File file = new File(sdCardDir, mFileName +".jpg");
            FileOutputStream fo;
            try {
                fo = new FileOutputStream(file);
                frameAtTime.compress(Bitmap.CompressFormat.JPEG, 90, fo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            try{
                fo.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return mFolderPath + mFileName + ".jpg";
        } else{
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        //new File(mFolderPath + mFileName).delete();
        if(mIsRecording) {
            handler.removeCallbacks(runnable);
            CaptureVideo();
        }
        Intent it = new Intent();
        it.putExtra("message", "用户取消");
        setResult(RESULT_ERROR_CODE, it);
        super.onBackPressed();
    }

    private void getResult(String path){
        Intent it = new Intent();
        it.putExtra("video", path);
        it.putExtra("length", String.valueOf(this.length));
        it.putExtra("degress", String.valueOf(this.degress));
        setResult(RESULT_OK, it);
        finish();
    }

}

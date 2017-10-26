package com.brlf.videorecord.camerafragment;

import android.support.annotation.Nullable;

import com.brlf.videorecord.camerafragment.listeners.CameraFragmentControlsListener;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentResultListener;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentStateListener;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentVideoRecordTextListener;

/*
 * Created by florentchampigny on 16/01/2017.
 */

public interface CameraFragmentApi {

    void takePhotoOrCaptureVideo(CameraFragmentResultListener resultListener, @Nullable String directoryPath, @Nullable String fileName);

    void openSettingDialog();

    void switchCameraTypeFrontBack();

    void switchActionPhotoVideo();

    void toggleFlashMode();

    void setStateListener(CameraFragmentStateListener cameraFragmentStateListener);

    void setTextListener(CameraFragmentVideoRecordTextListener cameraFragmentVideoRecordTextListener);

    void setControlsListener(CameraFragmentControlsListener cameraFragmentControlsListener);

    void setResultListener(CameraFragmentResultListener cameraFragmentResultListener);

}

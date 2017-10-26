package com.brlf.videorecord.camerafragment.internal.controller.view;

import android.support.annotation.Nullable;
import android.view.View;

import com.brlf.videorecord.camerafragment.configuration.Configuration;
import com.brlf.videorecord.camerafragment.internal.utils.Size;
import com.brlf.videorecord.camerafragment.listeners.CameraFragmentResultListener;

/*
 * Created by memfis on 7/6/16.
 */
public interface CameraView {

    void updateCameraPreview(Size size, View cameraPreview);

    void updateUiForMediaAction(@Configuration.MediaAction int mediaAction);

    void updateCameraSwitcher(int numberOfCameras);

    void onPhotoTaken(byte[] bytes, @Nullable CameraFragmentResultListener callback);

    void onVideoRecordStart(int width, int height);

    void onVideoRecordStop(@Nullable CameraFragmentResultListener callback);

    void releaseCameraPreview();

}

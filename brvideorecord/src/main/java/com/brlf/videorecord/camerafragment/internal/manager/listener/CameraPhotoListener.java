package com.brlf.videorecord.camerafragment.internal.manager.listener;

import com.brlf.videorecord.camerafragment.listeners.CameraFragmentResultListener;

import java.io.File;

/*
 * Created by memfis on 8/14/16.
 */
public interface CameraPhotoListener {
    void onPhotoTaken(byte[] bytes, File photoFile, CameraFragmentResultListener callback);

    void onPhotoTakeError();
}

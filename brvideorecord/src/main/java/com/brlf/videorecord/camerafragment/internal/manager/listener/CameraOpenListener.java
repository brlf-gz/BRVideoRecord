package com.brlf.videorecord.camerafragment.internal.manager.listener;

import com.brlf.videorecord.camerafragment.internal.utils.Size;

/*
 * Created by memfis on 8/14/16.
 */
public interface CameraOpenListener<CameraId, SurfaceListener> {
    void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

    void onCameraOpenError();
}

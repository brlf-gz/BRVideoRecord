package com.brlf.videorecord.camerafragment;

import android.Manifest;
import android.support.annotation.RequiresPermission;

import com.brlf.videorecord.camerafragment.configuration.Configuration;
import com.brlf.videorecord.camerafragment.internal.ui.BaseAnncaFragment;

public class CameraFragment extends BaseAnncaFragment {

    @RequiresPermission(Manifest.permission.CAMERA)
    public static CameraFragment newInstance(Configuration configuration) {
        return (CameraFragment) BaseAnncaFragment.newInstance(new CameraFragment(), configuration);
    }
}

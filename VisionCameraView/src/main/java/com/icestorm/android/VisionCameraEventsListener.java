package com.icestorm.android;


import android.graphics.Bitmap;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;

import java.util.List;


public interface VisionCameraEventsListener {
    void onCameraPictureTaken(String filePath);

    /**
     * @param bmp the bitmap image captured after every changed from the camera
     */
    void onCameraUpdated(Bitmap bmp);
}

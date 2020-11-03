package com.icestorm.android;


import android.graphics.Bitmap;


public interface VisionCameraEventsListener {
    void onCameraPictureTaken(String filePath);

    /**
     * @param bmp the bitmap image captured after every changed from the camera
     */
    void onCameraUpdated(Bitmap bmp);
}

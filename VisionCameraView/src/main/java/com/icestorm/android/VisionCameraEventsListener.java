package com.icestorm.android;


import com.google.mlkit.vision.text.Text;


public interface VisionCameraEventsListener {
    void onCameraPictureTaken(String filePath);
    void onBarcodeDetected(String value);
}

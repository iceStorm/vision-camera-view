package com.icestorm.android;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;

import java.util.List;

public interface MlkitResultListener {
    void onTextDetected(Text textBlocks);
    void onBarcodeDetected(String value);
    void onFaceDetected(List<Face> faces);
}

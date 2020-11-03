package com.icestorm.android.processor;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;

import java.util.List;


public interface MlkitResultListener {
    void onTextDetected(Text textBlocks);
    void onFaceDetected(List<Face> faces);
    void onBarcodeDetected(List<Barcode> barCodes);
}

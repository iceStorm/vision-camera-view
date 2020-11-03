package com.icestorm.android.processor;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.List;


public class MlkitScanner {
    private static final String TAG = "MlkitProcessor";


    public static void scanText(Bitmap bitmap, final MlkitResultListener listener) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image)
            .addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    if (text.getTextBlocks().size() == 0) {
                        Log.i(TAG, "onSuccess: no text found");
                        return;
                    }

                    listener.onTextDetected(text);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e);
                }
            });
    }

    public static void scanFace(Bitmap bitmap, final MlkitResultListener listener) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions processFace = new FaceDetectorOptions.Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();

        FaceDetector recognizer = FaceDetection.getClient(processFace);
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if (faces.size() == 0) {
                            Log.i(TAG, "onSuccess: no face found");
                            return;
                        }

                        listener.onFaceDetected(faces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });
    }

    public static void scanBarCode(Bitmap bmp, final MlkitResultListener listener) {
        InputImage image = InputImage.fromBitmap(bmp, 0);

        BarcodeScannerOptions builder = new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_EAN_13)
            .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(builder);
        scanner.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    if (barcodes.size() == 0) {
                        Log.i(TAG, "onSuccess: no barcode found");
                        return;
                    }

                    listener.onBarcodeDetected(barcodes);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
    }

}

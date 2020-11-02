package com.icestorm.android.processor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.icestorm.android.BarcodeListener;
import com.icestorm.android.TextListener;
import com.icestorm.android.mlkit.FaceContourGraphic;
import com.icestorm.android.mlkit.GraphicOverlay;
import com.icestorm.android.mlkit.TextGraphic;

import java.util.List;

public class MlkitProcessor {
    private static final String TAG = "MlkitProcessor";


    public static void processText(Bitmap bitmap, final GraphicOverlay overlay, final boolean showTextBounder, final int textColor, final float textSize, final TextListener listener) {
        overlay.clear();
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


                    for (Text.TextBlock paragraph : text.getTextBlocks())
                        for (Text.Line line : paragraph.getLines())
                            for (Text.Element word : line.getElements()) {
                                Log.i(TAG, "onSuccess: " + word.getText());

                                TextGraphic graphic = new TextGraphic(overlay, word, textColor, textSize, showTextBounder);
                                overlay.add(graphic);
                            }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: ", e);
                }
            });
    }

    public static void processFace(Bitmap bitmap, final GraphicOverlay overlay) {
        overlay.clear();
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions processFace = new FaceDetectorOptions.Builder()
                .setLandmarkMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
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

                        for (Face face : faces) {
                            FaceContourGraphic graphic = new FaceContourGraphic(overlay);
                            overlay.add(graphic);
                            graphic.updateFace(face);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });
    }

    public static void scanBarCode(Bitmap bmp, final BarcodeListener listener) {
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

                    for (Barcode b : barcodes) {
                        listener.onBarcodeDetected(b.getRawValue());
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
    }

}

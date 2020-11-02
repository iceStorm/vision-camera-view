package com.icestorm.android.processor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.icestorm.android.mlkit.FaceContourGraphic;
import com.icestorm.android.mlkit.GraphicOverlay;
import com.icestorm.android.mlkit.TextGraphic;

import java.util.List;

public class MlkitProcessor {
    private static final String TAG = "MlkitProcessor";
    public enum ProcessingType {FACE, TEXT};


    public static void process(ProcessingType type, Bitmap bmp, GraphicOverlay overlay) {
        overlay.clear();

        switch (type) {
            case FACE: {
                processFace(bmp, overlay);
                break;
            }

            case TEXT: {
                processText(bmp, overlay);
                break;
            }
        }
    }


    private static void processText(Bitmap bitmap, final GraphicOverlay overlay) {
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

                    for (Text.TextBlock paragraph : text.getTextBlocks())
                        for (Text.Line line : paragraph.getLines())
                            for (Text.Element word : line.getElements()) {
                                Log.i(TAG, "onSuccess: " + word.getText());

                                TextGraphic graphic = new TextGraphic(overlay, word, Color.RED, true);
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

    private static void processFace(Bitmap bitmap, final GraphicOverlay overlay) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions processFace = new FaceDetectorOptions.Builder()
                .setLandmarkMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
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
                            graphic.updateFace(face);
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

}

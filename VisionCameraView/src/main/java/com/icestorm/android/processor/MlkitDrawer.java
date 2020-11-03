package com.icestorm.android.processor;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;
import com.icestorm.android.mlkit.FaceContourGraphic;
import com.icestorm.android.mlkit.GraphicOverlay;
import com.icestorm.android.mlkit.TextGraphic;

import java.util.List;


public class MlkitDrawer {


    public static void drawTexts(boolean clearBefore, Text textBlock, GraphicOverlay overlay, int textColor, float textSize, boolean drawTextBounder) {
        if (clearBefore)
            overlay.clear();


        for (Text.TextBlock paragraph : textBlock.getTextBlocks())
            for (Text.Line line : paragraph.getLines())
                for (Text.Element word : line.getElements()) {
                    TextGraphic graphic = new TextGraphic(overlay, word, textColor, textSize, drawTextBounder);
                    overlay.add(graphic);
                }
    }

    public static void drawFaces(boolean clearBefore, List<Face> faces, GraphicOverlay overlay, int pointColor, float faceContourPointRadius) {
        if (clearBefore)
            overlay.clear();


        for (Face f : faces) {
            FaceContourGraphic graphic = new FaceContourGraphic(overlay, pointColor, faceContourPointRadius);
            overlay.add(graphic);
            graphic.updateFace(f);
        }
    }


}

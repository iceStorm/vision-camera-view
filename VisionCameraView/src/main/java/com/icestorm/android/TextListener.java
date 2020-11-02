package com.icestorm.android;

import com.google.mlkit.vision.text.Text;

public interface TextListener {
    void onTextDetected(Text textBlocks);
}

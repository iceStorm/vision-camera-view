package com.icestorm.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class CameraImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public CameraImageButton(@NonNull Context context) {
        super(context);
    }

    public CameraImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraImageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void setEnabled(boolean enabled) {
        this.setImageAlpha(enabled ? 0xFF : 0x3F);
    }

}

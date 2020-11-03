package com.icestorm.android;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;



public class ScannerOverlay extends RelativeLayout {
    public View line;
    public View box;
    public View background;


    public ScannerOverlay(Context context) {
        super(context);
    }

    public ScannerOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScannerOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScannerOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }



    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View screen = inflater.inflate(R.layout.scanner_overlay_layout, this, true);

        line = screen.findViewById(R.id.scanningLine);
        box = screen.findViewById(R.id.targetingBox);
        background = screen.findViewById(R.id.boxBackground);


        box.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AdditiveAnimator
                    .animate(line, 5000)
                    .setInterpolator(new DecelerateInterpolator())
                    .x(0)
                    .y(box.getHeight())
                    .setRepeatCount(Animation.INFINITE)
                    .setRepeatMode(Animation.REVERSE)
                    .start();
            }
        });
    }


}

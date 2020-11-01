package com.icestorm.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;



public class VisionCameraView extends SurfaceView {
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#F44336");
    private static final int DEFAULT_FACE_COLOR = Color.parseColor("#007BFF");

    private int faceColor;
    private int textColor;
    private Paint painter;



    public VisionCameraView(Context context) {
        super(context);
        init(context, null);
    }

    public VisionCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VisionCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public VisionCameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }



    private void init(Context context, AttributeSet attrs) {
        painter = new Paint();
        painter.setColor(Color.RED);
        painter.setStrokeWidth(30f);
        painter.setTextSize(20f);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VisionCameraView);

            this.textColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_TEXT_COLOR);
            this.faceColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_FACE_COLOR);

            array.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText("Vision Camera View", 0, 0, painter);
//        super.onDraw(canvas);
    }




    public void switchOrientation() {

    }

    public Bitmap captureImage() throws Exception {
        throw new Exception("Not implemented");
    }
}

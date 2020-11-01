package com.icestorm.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class VisionCameraView extends SurfaceView
        implements SurfaceHolder.Callback, VisionCameraPermissionResult {
    private static final String TAG = "VisionCameraView";

    /* CODES */
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;

    /* constants */
    private static final int DEFAULT_CAMERA_ID = 0;
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#F44336");
    private static final int DEFAULT_FACE_COLOR = Color.parseColor("#007BFF");
    private static final boolean IS_SHOW_FACE = true;
    private static final boolean IS_SHOW_TEXT = true;
    private static final boolean IS_SHOW_TEXT_BORDER = true;

    /* properties */
    private Bitmap currentBitmapImage;
    private boolean isFrontCamera;
    private int faceColor;
    private int textColor;
    private boolean showFace;
    private boolean showText;
    private boolean showTextBorder;

    /* fields */
    private Paint textPainter;
    private Paint facePainter;
    private float aspectRatio = 0f;
    private Camera camera;
    private Context context;
    private AttributeSet attrs;
    private SurfaceHolder surfaceHolder;



    /* constructors */
    public VisionCameraView(Context context) {
        super(context);

        setWillNotDraw(false);
        init(context, null);
    }

    public VisionCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
        init(context, attrs);
    }

    public VisionCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);
        init(context, attrs);
    }

    public VisionCameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setWillNotDraw(false);
        init(context, attrs);
    }



    /* initializing */
    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        this.attrs = attrs;

        if (!isInEditMode()) {
            initCamera();
        }
        initAttributes();
    }

    private void initCamera() {
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
    }

    private void initAttributes() {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VisionCameraView);

            this.isFrontCamera = array.getBoolean(R.styleable.VisionCameraView_vc_isFrontCamera, false);
            this.textColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_TEXT_COLOR);
            this.faceColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_FACE_COLOR);
            this.showFace = array.getBoolean(R.styleable.VisionCameraView_vc_showFace, IS_SHOW_FACE);
            this.showText = array.getBoolean(R.styleable.VisionCameraView_vc_showText, IS_SHOW_TEXT);
            this.showTextBorder = array.getBoolean(R.styleable.VisionCameraView_vc_showTextBorder, IS_SHOW_TEXT_BORDER);

            textPainter = new Paint();
            textPainter.setColor(this.textColor);
            textPainter.setStrokeWidth(30f);
            textPainter.setStyle(Paint.Style.STROKE);
            textPainter.setTextSize(20f);

            facePainter = new Paint();
            textPainter.setColor(this.faceColor);
            textPainter.setStrokeWidth(30f);
            textPainter.setStyle(Paint.Style.STROKE);
            textPainter.setTextSize(20f);

            array.recycle();
        }

        Log.i(TAG, "initAttributes: ");
    }



    /* Parent methods overriding */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);


        if (this.aspectRatio == 0f)
            setMeasuredDimension(width, height);
        else {
            int newWidth;
            int newHeight;
            float actualRatio = width > height ? aspectRatio : 1f/aspectRatio;

            if (width < height * actualRatio) {
                newHeight = height;
                newWidth = Math.round(height * actualRatio);
            } else {
                newHeight = height;
                newWidth = Math.round(width / actualRatio);
            }

            Log.d(TAG, String.format("Measured dimensions set: %d x %d", newWidth, newHeight));
            setMeasuredDimension(newWidth, newHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawText("Vision Camera View", 50, 50, textPainter);
        super.onDraw(canvas);

        Log.i(TAG, "onDraw: ");
    }



    /* public utility functions */
    public void switchCamera() throws IOException {
        if (this.isFrontCamera) {
            this.isFrontCamera = false;
            camera.open(0);
            camera.reconnect();
        }
        else {
            this.isFrontCamera = true;
            camera.open(1);
            camera.reconnect();
        }
    }

    public Bitmap captureImage() {
        return currentBitmapImage;
    }

    private void openCamera() {
        try {
            if (isCameraPermissionGranted()) {
                camera = Camera.open(DEFAULT_CAMERA_ID);
                camera.setDisplayOrientation(90);

                Camera.Parameters param;
                param = camera.getParameters();
                List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();

                param.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
                camera.setParameters(param);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();

                /* set the callback whenever content in camera is changed */
                /*camera.setPreviewCallback(livePreviewCallback);*/
            }
            else {
                askCameraPermission();
            }
        }
        catch (Exception e) {
            Toast.makeText(context, "Error on surfaceCreated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "surfaceCreated: ", e);
        }
    }

    private Camera.PreviewCallback livePreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

            byte[] bytes = out.toByteArray();
            currentBitmapImage= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    };



    /* SurfaceHolder Callbacks */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        openCamera();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }



    /* Requesting camera permission */
    private boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }

        return PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
    }

    private void askCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context instanceof Activity) {
                ((Activity)context).requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }

            return;
        }

        Toast.makeText(context, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPermissionGranted() {
        openCamera();
    }

}

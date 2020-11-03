package com.icestorm.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;
import com.icestorm.android.mlkit.GraphicOverlay;
import com.icestorm.android.processor.MlkitDrawer;
import com.icestorm.android.processor.MlkitResultListener;
import com.icestorm.android.processor.MlkitScanner;
import com.icestorm.android.utils.CameraImageResizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


@SuppressWarnings("deprecation")
public class VisionCameraView extends RelativeLayout
        implements SurfaceHolder.Callback,
            Camera.PictureCallback,
            Camera.ShutterCallback,
            Camera.PreviewCallback,
            MlkitResultListener {


    private static final String TAG = "VisionCameraView";

    /* CODES */
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 0;

    /* constants */
    private static final boolean DEFAULT_OPTIMAL_SIZE = false;
    private static final float DEFAULT_FACE_CONTOUR_RADIUS = 5f;
    private static final int DEFAULT_FACE_COLOR = Color.parseColor("#007BFF");

    private static final int DEFAULT_QR_BACKGROUND_COLOR = Color.parseColor("#4AF44336");
    private static final int DEFAULT_QR_LINE_COLOR = Color.parseColor("#F44336");
    private static final float DEFAULT_QR_LINE_HEIGHT = 2f;
    private static final float DEFAULT_QR_SIZE = 200f;

    private static final float DEFAULT_TEXT_SIZE = 30f;
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#F44336");
    private static final boolean IS_SHOW_TEXT_BORDER = false;


    /* properties */
    private AlertDialog alertDialog;
    private boolean isFrontCamera;
    public float faceContourPointRadius;
    public int qrLineColor;
    public float qrLineHeight;
    public int qrBackgroundColor;
    public float qrSize;
    public int faceColor;
    public int textColor;
    public float textSize;
    public boolean isShowTextBorder;
    private boolean isScanFace;
    private boolean isScanText;
    private boolean isScanQR;
    private boolean isAutoScan;
    private boolean isOptimalSize;
    public GraphicOverlay graphicOverlay;

    /* fields */
    private RelativeLayout rootView;
    private SurfaceView surfaceView;
    private CameraImageButton btnScanText;
    private CameraImageButton btnScanFace;
    private CameraImageButton btnScanQR;
    private CameraImageButton btnSwitchCamera;
    private CameraImageButton btnTakePicture;
    private CameraImageButton btnClear;
    private ScannerOverlay scannerOverlay;
    private Camera camera;
    private Context context;
    private AttributeSet attrs;
    private SurfaceHolder surfaceHolder;
    private CameraHandlerThread cameraHandlerThread;



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
        LayoutInflater inflater = LayoutInflater.from(context);
        View screen = inflater.inflate(R.layout.vision_camera_layout, this, true);

        rootView = (RelativeLayout) screen;
        scannerOverlay = screen.findViewById(R.id.scannerOverlay);
        surfaceView = screen.findViewById(R.id.surfaceView);
        btnScanFace = screen.findViewById(R.id.btnScanFace);
        btnScanText = screen.findViewById(R.id.btnScanText);
        btnScanQR = screen.findViewById(R.id.btnScanQR);
        btnSwitchCamera = screen.findViewById(R.id.btnSwitchCamera);
        btnTakePicture = screen.findViewById(R.id.btnTakePicture);
        btnClear = screen.findViewById(R.id.btnClear);

        
        this.context = context;
        this.attrs = attrs;


        if (!isInEditMode()) {
            assignEvents();
            initAlertDialog(inflater);

            this.surfaceHolder = surfaceView.getHolder();
            this.surfaceHolder.addCallback(this);
            this.setFocusable(true);
            this.graphicOverlay = screen.findViewById(R.id.graphicOverlay);
        }


        initAttributes();   /* must call before all other operations */
    }

    private void initAlertDialog(LayoutInflater inflater) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View layout = inflater.inflate(R.layout.saving_image_alert_layout, null);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
    }

    private void initAttributes() {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VisionCameraView);

            this.isOptimalSize = array.getBoolean(R.styleable.VisionCameraView_vc_isOptimalSize, DEFAULT_OPTIMAL_SIZE);
            this.isAutoScan = array.getBoolean(R.styleable.VisionCameraView_vc_autoScan, true);
            this.isFrontCamera = array.getBoolean(R.styleable.VisionCameraView_vc_isFrontCamera, false);
            this.isScanFace = array.getBoolean(R.styleable.VisionCameraView_vc_isScanFace, false);
            this.isScanText = array.getBoolean(R.styleable.VisionCameraView_vc_isScanText, true);
            this.isScanQR = array.getBoolean(R.styleable.VisionCameraView_vc_isScanQR, false);

            this.qrBackgroundColor = array.getColor(R.styleable.VisionCameraView_vc_qrBackgroundColor, DEFAULT_QR_BACKGROUND_COLOR);
            this.qrSize = array.getDimension(R.styleable.VisionCameraView_vc_qrSize, DEFAULT_QR_SIZE);
            this.qrLineColor = array.getColor(R.styleable.VisionCameraView_vc_qrLineColor, DEFAULT_QR_LINE_COLOR);
            this.qrLineHeight = array.getDimension(R.styleable.VisionCameraView_vc_qrLineHeight, DEFAULT_QR_LINE_HEIGHT);

            this.faceColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_FACE_COLOR);
            this.faceContourPointRadius = array.getDimension(R.styleable.VisionCameraView_vc_faceContourPointRadius, DEFAULT_FACE_CONTOUR_RADIUS);

            this.textColor = array.getColor(R.styleable.VisionCameraView_vc_textColor, DEFAULT_TEXT_COLOR);
            this.textSize = array.getDimension(R.styleable.VisionCameraView_vc_textSize, DEFAULT_TEXT_SIZE);
            this.isShowTextBorder = array.getBoolean(R.styleable.VisionCameraView_vc_showTextBorder, IS_SHOW_TEXT_BORDER);

            array.recycle();
        }

        /* initialize the state of each view beyond its attributes in xml */
        initViewsState();
        Log.i(TAG, "initAttributes: ");
    }

    private void initViewsState() {
        if (!isScanQR) {
            btnScanQR.setEnabled(false);
            scannerOverlay.setVisibility(GONE);

            if (!isScanText) {
                btnScanText.setEnabled(false);
            }

            if (!isScanFace) {
                btnScanFace.setEnabled(false);
            }
        }
        else {
            isScanFace = false;
            isScanText = false;
            btnScanText.setEnabled(false);
            btnScanFace.setEnabled(false);

            scannerOverlay.setVisibility(VISIBLE);
        }


        if (!isAutoScan) {
            scannerOverlay.setVisibility(GONE);
            btnScanFace.setVisibility(GONE);
            btnScanText.setVisibility(GONE);
            btnScanQR.setVisibility(GONE);
        }
        else {
            scannerOverlay.line.setBackgroundColor(qrLineColor);
            scannerOverlay.background.setBackgroundColor(qrBackgroundColor);

            ViewGroup.LayoutParams lineParams = scannerOverlay.line.getLayoutParams();
            lineParams.height = (int)qrLineHeight;
            scannerOverlay.line.setLayoutParams(lineParams);

            ViewGroup.LayoutParams boxParams = scannerOverlay.box.getLayoutParams();
            boxParams.width = (int)qrSize;
            boxParams.height = (int)qrSize;
            scannerOverlay.box.setLayoutParams(boxParams);
        }
    }

    private void assignEvents() {
        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                graphicOverlay.clear();
            }
        });
        btnSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        btnScanQR.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                graphicOverlay.clear();
                isScanQR = !isScanQR;
                btnScanQR.setEnabled(isScanQR);

                if (isScanQR) {
                    isScanFace = false;
                    isScanText = false;
                    btnScanFace.setEnabled(false);
                    btnScanText.setEnabled(false);
                }


                scannerOverlay.setVisibility(isScanQR ? VISIBLE: GONE);
            }
        });

        btnScanFace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                graphicOverlay.clear();
                isScanFace = !isScanFace;
                btnScanFace.setEnabled(isScanFace);

                if (isScanQR) {
                    isScanQR = false;
                    btnScanQR.setEnabled(false);
                }

                if (isScanText) {
                    isScanText = false;
                    btnScanText.setEnabled(false);
                }


                scannerOverlay.setVisibility(isScanQR ? VISIBLE: GONE);
            }
        });

        btnScanText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                graphicOverlay.clear();
                isScanText = !isScanText;
                btnScanText.setEnabled(isScanText);

                if (isScanQR) {
                    isScanQR = false;
                    btnScanQR.setEnabled(false);
                    graphicOverlay.clear();
                }

                if (isScanFace) {
                    isScanFace = false;
                    btnScanFace.setEnabled(false);
                }

                scannerOverlay.setVisibility(isScanQR ? VISIBLE: GONE);
            }
        });


        btnTakePicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                beginCaptureImage();
            }
        });
    }



    /* initializing camera */
    private void oldOpenCamera(final int cameraId) {
        try {
            camera = Camera.open(cameraId);
            initCamera();
        }
        catch (RuntimeException e) {
            Log.e(TAG, "failed to open front camera");
        }
    }

    private void newOpenCamera(final int cameraId) {
        if (isCameraPermissionGranted()) {
            if (cameraHandlerThread == null) {
                cameraHandlerThread = new CameraHandlerThread();
            }

            synchronized (cameraHandlerThread) {
                cameraHandlerThread.openCamera(cameraId);
            }
        }
        else {
            askCameraPermission();
        }
    }

    private void initCamera() {
        try {
            graphicOverlay.clear();
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(VisionCameraView.this);

            /* set the optimal size */
            if (this.isOptimalSize) {
                final Camera.Parameters param;
                param = camera.getParameters();

                List<Camera.Size> mSupportedPreviewSizes = param.getSupportedPreviewSizes();
                Camera.Size mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, getWidth(), getHeight());
                param.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                camera.setParameters(param);
            }

            camera.startPreview();
        }
        catch (Exception e) {
            Toast.makeText(context, "Failed to init the camera", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "initCamera: ", e);
        }
    }



    /* public util functions */
    public void switchCamera() {
        if (this.isFrontCamera) {
            this.isFrontCamera = false;
            newOpenCamera(0);
        }
        else {
            this.isFrontCamera = true;
            newOpenCamera(1);
        }
    }

    public void beginCaptureImage() {
        alertDialog.show();
        camera.takePicture(null, null, VisionCameraView.this);
    }



    /* camera callbacks */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "onPictureTaken: " + data.length);


        try {
            String filePath = context.getFilesDir().getAbsolutePath() + "/temp.jpg";
            File f = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(f);

            outputStream.write(getPortraitBytesArray(data));
            outputStream.flush();
            outputStream.close();


            if (context instanceof VisionCameraEventsListener) {
                ((VisionCameraEventsListener) context).onCameraPictureTaken(filePath);
                alertDialog.dismiss();
                camera.startPreview();
            }
        } catch (Exception e) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onPictureTaken: ", e);
        }
    }

    @Override
    public void onShutter() {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        try {
            byte[] yuvBytes = getYuvBytesArray(data, camera);
            byte[] portraitBytes = getPortraitBytesArray(yuvBytes);

            Bitmap actualBitmap = BitmapFactory.decodeByteArray(portraitBytes, 0, portraitBytes.length);
            Bitmap resizedBitmap = new CameraImageResizer(actualBitmap, rootView).getResizedImage();


            if (context instanceof VisionCameraEventsListener)
                ((VisionCameraEventsListener) context).onCameraUpdated(resizedBitmap);


            if (isAutoScan)
                if (isScanFace)
                    MlkitScanner.scanFace(resizedBitmap, VisionCameraView.this);
                else
                if (isScanText)
                    MlkitScanner.scanText(resizedBitmap, VisionCameraView.this);
                else
                if (isScanQR)
                    MlkitScanner.scanBarCode(resizedBitmap, VisionCameraView.this);

        }
        catch (Exception e) {
            Toast toast = Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }



    /* image data processing */
    private byte[] getPortraitBytesArray(byte[] data) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bmp == null) return null;


        int width = bmp.getWidth();
        int height = bmp.getHeight();


        Matrix matrix = new Matrix();
        matrix.postRotate(isFrontCamera? -90: 90);

        Bitmap rotatedImg = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        bmp.recycle();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        rotatedImg.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);


        rotatedImg.recycle();
        return outputStream.toByteArray();
    }

    private byte[] getYuvBytesArray(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, bos);
        return bos.toByteArray();
    }

    private void saveToImage(byte[] data) {
        if (data == null || data.length == 0) {
            Log.i(TAG, "saveToImage: byte[] is empty");
            return;
        }

        try {
            String filePath = context.getFilesDir().getAbsolutePath() + "/stream.jpg";
            File f = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(f);

            outputStream.write(data);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(context, "New image saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onPictureTaken: ", e);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }

        return optimalSize;
    }



    /* SurfaceHolder Callbacks */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        newOpenCamera(isFrontCamera? 1: 0);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        /*Toast.makeText(context, "surfaceChanged", Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        camera.stopPreview();
        /*camera.release();*/
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

    public void onCameraPermissionGranted() {
        newOpenCamera(isFrontCamera? 1: 0);
    }



    /* overriding results from MlkitScanner --> send to activity */
    @Override
    public void onBarcodeDetected(List<Barcode> barCodes) {
        MlkitDrawer.drawBarCodes(true, barCodes, graphicOverlay, textColor, textSize, isShowTextBorder);
    }

    @Override
    public void onFaceDetected(List<Face> faces) {
        MlkitDrawer.drawFaces(true, faces, graphicOverlay, faceColor, faceContourPointRadius);
    }

    @Override
    public void onTextDetected(Text textBlocks) {
        MlkitDrawer.drawTexts(true, textBlocks, graphicOverlay, textColor, textSize, isShowTextBorder);
    }



    /* Handling the camera in other thread */
    public class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;


        public CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera(final int cameraId) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldOpenCamera(cameraId);
                    notifyCameraOpened();
                }
            });

            try {
                wait();
            }
            catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
            }
        }
    }

}

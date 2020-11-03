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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;
import com.icestorm.android.mlkit.GraphicOverlay;
import com.icestorm.android.processor.MlkitDrawer;
import com.icestorm.android.processor.MlkitScanner;
import com.icestorm.android.utils.CameraImageResizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import info.androidhive.barcode.ScannerOverlay;


@SuppressWarnings("deprecation")
public class VisionCameraView extends RelativeLayout
        implements SurfaceHolder.Callback,
            Camera.PictureCallback,
            Camera.ShutterCallback,
            MlkitResultListener {


    private static final String TAG = "VisionCameraView";

    /* CODES */
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 0;

    /* constants */
    private static final float DEFAULT_FACE_CONTOUR_RADIUS = 5f;
    private static final int DEFAULT_TEXT_COLOR = Color.parseColor("#F44336");
    private static final int DEFAULT_FACE_COLOR = Color.parseColor("#007BFF");
    private static final float DEFAULT_TEXT_SIZE = 30f;
    private static final boolean IS_SHOW_TEXT_BORDER = false;

    /* properties */
    private AlertDialog alertDialog;
    private boolean isFrontCamera;
    public float faceContourPointRadius;
    public int faceColor;
    public int textColor;
    public float textSize;
    public boolean isShowTextBorder;
    private boolean isScanFace;
    private boolean isScanText;
    private boolean isScanQR;
    private boolean isAutoScan;
    public GraphicOverlay graphicOverlay;

    /* fields */
    private RelativeLayout rootView;
    private SurfaceView surfaceView;
    private CameraImageButton btnScanText;
    private CameraImageButton btnScanFace;
    private CameraImageButton btnScanQR;
    private CameraImageButton btnSwitchCamera;
    private CameraImageButton btnTakePicture;
    private ScannerOverlay scannerOverlay;
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

        
        this.context = context;
        this.attrs = attrs;


        if (!isInEditMode()) {
            assignEvents();
            initAlertDialog(inflater);

            this.surfaceHolder = surfaceView.getHolder();
            this.surfaceHolder.addCallback(this);
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

            this.isAutoScan = array.getBoolean(R.styleable.VisionCameraView_vc_autoScan, true);
            this.isFrontCamera = array.getBoolean(R.styleable.VisionCameraView_vc_isFrontCamera, false);
            this.isScanFace = array.getBoolean(R.styleable.VisionCameraView_vc_isScanFace, false);
            this.isScanText = array.getBoolean(R.styleable.VisionCameraView_vc_isScanText, true);
            this.isScanQR = array.getBoolean(R.styleable.VisionCameraView_vc_isScanQR, false);

            this.faceContourPointRadius = array.getDimension(R.styleable.VisionCameraView_vc_faceContourPointRadius, DEFAULT_FACE_CONTOUR_RADIUS);
            this.textColor = array.getColor(R.styleable.VisionCameraView_vc_textColor, DEFAULT_TEXT_COLOR);
            this.faceColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_FACE_COLOR);
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
            btnScanFace.setVisibility(GONE);
            btnScanText.setVisibility(GONE);
            btnScanQR.setVisibility(GONE);
        }
    }

    private void assignEvents() {

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



    /* public utility functions */
    private void openCamera(int cameraId) {
        try {
            if (isCameraPermissionGranted()) {
                graphicOverlay.clear();
                camera = Camera.open(cameraId);
                camera.setDisplayOrientation(90);

                final Camera.Parameters param;
                param = camera.getParameters();

                List<Camera.Size> mSupportedPreviewSizes = param.getSupportedPreviewSizes();
                for(Camera.Size str: mSupportedPreviewSizes)
                    Log.e(TAG, str.width + "/" + str.height);

                Camera.Size mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, getWidth(), getHeight());
                param.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                camera.setParameters(param);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();


                /* set the callback whenever content in camera is changed */
                camera.setPreviewCallback(new Camera.PreviewCallback() {
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
                });
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

    public void switchCamera() {
        if (this.isFrontCamera) {
            this.isFrontCamera = false;
            openCamera(0);
        }
        else {
            this.isFrontCamera = true;
            openCamera(1);
        }
    }

    public void beginCaptureImage() {
        alertDialog.show();
        camera.takePicture(null, null, VisionCameraView.this);
    }

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

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }



    /* SurfaceHolder Callbacks */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        openCamera(isFrontCamera? 1: 0);
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

    public void onCameraPermissionGranted() {
        openCamera(isFrontCamera? 1: 0);
    }



    /* overriding results from MlkitScanner --> send to activity */
    @Override
    public void onBarcodeDetected(String value) {
    }

    @Override
    public void onFaceDetected(List<Face> faces) {
        MlkitDrawer.drawFaces(true, faces, graphicOverlay, faceColor, faceContourPointRadius);
    }

    @Override
    public void onTextDetected(Text textBlocks) {
        MlkitDrawer.drawTexts(true, textBlocks, graphicOverlay, textColor, textSize, isShowTextBorder);
    }

}

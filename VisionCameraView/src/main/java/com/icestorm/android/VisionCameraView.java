package com.icestorm.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.icestorm.android.databinding.VisionCameraLayoutBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SuppressWarnings("deprecation")
public class VisionCameraView extends RelativeLayout
        implements SurfaceHolder.Callback,
            VisionCameraPermissionResult,
            Camera.PictureCallback,
            Camera.ShutterCallback {


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
    private AlertDialog alertDialog;
    private Bitmap currentBitmapImage;
    private boolean isFrontCamera;
    private int faceColor;
    private int textColor;
    private boolean showFace;
    private boolean showText;
    private boolean showTextBorder;
    private boolean isLightTurnedOn;
    private boolean isScanFace;
    private boolean isScanText;
    private boolean isScanQR;

    /* fields */
    private VisionCameraLayoutBinding B;
    private Set<Text.Element> words = new HashSet<>();
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
        LayoutInflater inflater = LayoutInflater.from(context);
        B = DataBindingUtil.inflate(inflater, R.layout.vision_camera_layout, this, true);

        this.context = context;
        this.attrs = attrs;

        if (!isInEditMode()) {
            initCamera();
            assignEvents();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout = inflater.inflate(R.layout.saving_image_alert_layout, null);
            builder.setView(layout);
            alertDialog = builder.create();
        }

        initAttributes();
    }

    private void initCamera() {
        this.surfaceHolder = B.surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
    }

    private void initAttributes() {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VisionCameraView);

            this.isFrontCamera = array.getBoolean(R.styleable.VisionCameraView_vc_isFrontCamera, false);
            this.isLightTurnedOn = array.getBoolean(R.styleable.VisionCameraView_vc_isLightTurnedOn, false);
            this.isScanFace = array.getBoolean(R.styleable.VisionCameraView_vc_isScanFace, true);
            this.isScanText = array.getBoolean(R.styleable.VisionCameraView_vc_isScanText, false);
            this.isScanQR = array.getBoolean(R.styleable.VisionCameraView_vc_isScanQR, false);


            this.textColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_TEXT_COLOR);
            this.faceColor = array.getColor(R.styleable.VisionCameraView_vc_faceColor, DEFAULT_FACE_COLOR);
            this.showFace = array.getBoolean(R.styleable.VisionCameraView_vc_showFace, IS_SHOW_FACE);
            this.showText = array.getBoolean(R.styleable.VisionCameraView_vc_showText, IS_SHOW_TEXT);
            this.showTextBorder = array.getBoolean(R.styleable.VisionCameraView_vc_showTextBorder, IS_SHOW_TEXT_BORDER);

            textPainter = new Paint();
            textPainter.setColor(this.textColor);
            textPainter.setTextSize(30f);

            facePainter = new Paint();
            textPainter.setColor(this.faceColor);
            textPainter.setStrokeWidth(30f);
            textPainter.setStyle(Paint.Style.STROKE);
            textPainter.setTextSize(20f);

            array.recycle();
        }

        /* initialize the state of each view beyond its attributes in xml */
        initViewsState();
        Log.i(TAG, "initAttributes: ");
    }

    private void initViewsState() {
        if (!isScanQR) {
            B.btnScanQR.setEnabled(false);

            if (!isScanText) {
                B.btnScanText.setEnabled(false);
            }

            if (!isScanFace) {
                B.btnScanFace.setEnabled(false);
            }
        }
        else {
            isScanFace = false;
            isScanText = false;
            B.btnScanText.setEnabled(false);
            B.btnScanFace.setEnabled(false);
        }
    }

    private void assignEvents() {
        B.btnSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });


        /*B.btnToggleLight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters params = camera.getParameters();

                if (isLightTurnedOn) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    B.btnSwitchCamera.setImageResource(R.drawable.ic_turn_off);
                    isLightTurnedOn = false;
                } else {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    B.btnSwitchCamera.setImageResource(R.drawable.ic_turn_on);
                    isLightTurnedOn = true;
                }

                camera.setParameters(params);
            }
        });*/

        B.btnScanQR.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanQR = !isScanQR;
                B.btnScanQR.setEnabled(isScanQR);

                if (isScanQR) {
                    isScanFace = false;
                    isScanText = false;
                    B.btnScanFace.setEnabled(false);
                    B.btnScanText.setEnabled(false);
                }
            }
        });

        B.btnScanFace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanFace = !isScanFace;
                B.btnScanFace.setEnabled(isScanFace);

                if (isScanQR) {
                    isScanQR = false;
                    B.btnScanQR.setEnabled(false);
                }
            }
        });

        B.btnScanText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanText = !isScanText;
                B.btnScanText.setEnabled(isScanText);

                if (isScanQR) {
                    isScanQR = false;
                    B.btnScanQR.setEnabled(false);
                }
            }
        });


        B.btnTakePicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                beginCaptureImage();
            }
        });
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
        super.onDraw(canvas);

        /*for (Text.Element word: words) {
            RectF rect = new RectF(word.getBoundingBox());
            canvas.drawText(word.getText(), rect.left, rect.bottom, textPainter);
        }*/
    }



    /* public utility functions */
    private void openCamera(int cameraId) {
        try {
            if (isCameraPermissionGranted()) {
                camera = Camera.open(cameraId);
                camera.setDisplayOrientation(90);

                Camera.Parameters param;
                param = camera.getParameters();

                List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();
                param.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
                camera.setParameters(param);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();


                /* set the callback whenever content in camera is changed */
                /*camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        saveToImage(data);
                    }
                });*/
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

            outputStream.write(getPortraitByteArray(data));
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
    private byte[] getPortraitByteArray(byte[] data) {
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

    private Bitmap getPortraitBitmap(byte[] data, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        int w = params.getPreviewSize().width;
        int h = params.getPreviewSize().height;
        Rect r = new Rect(0, 0, w, h);

        Bitmap bitmap = Bitmap.createBitmap(r.width(), r.height(), Bitmap.Config.ARGB_8888);
        Allocation bmData = renderScriptNV21ToRGBA888(
                context,
                r.width(),
                r.height(),
                data);

        bmData.copyTo(bitmap);
        return bitmap;
    }

    public Allocation renderScriptNV21ToRGBA888(Context context, int width, int height, byte[] nv21) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        return out;
    }

    private void drawTextGraphics() {
        InputImage inputImage = InputImage.fromBitmap(currentBitmapImage, 0);
        if (inputImage == null) return;

        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(inputImage)
            .addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    if (text.getTextBlocks().size() == 0) {
                        Log.i(TAG, "onSuccess: no text found");
                        return;
                    }

                    if (context instanceof VisionCameraEventsListener) {
                        ((VisionCameraEventsListener) context).onTextDetected(text);
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
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

            byte[] portraitArray = getPortraitByteArray(data);
            if (portraitArray == null || portraitArray.length == 0) {
                outputStream.close();
                return;
            }

            outputStream.write(portraitArray);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(context, "New image saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onPictureTaken: ", e);
        }
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

    @Override
    public void onCameraPermissionGranted() {
        openCamera(isFrontCamera? 1: 0);
    }

}

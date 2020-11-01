package com.example.visiondemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.example.visiondemo.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;
    private static final int CAMERA_INTENT_REQUEST_CODE = 1;

    private ActivityMainBinding B;
    private SurfaceHolder surfaceHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = DataBindingUtil.setContentView(this, R.layout.activity_main);

        surfaceHolder = B.cameraView.getHolder();
        surfaceHolder.addCallback(this);
    }


    private void openCamera() {
        try {
            if (isCameraPermissionGranted()) {
                /*camera = Camera.open();
                camera.setDisplayOrientation(90);

                Camera.Parameters param;
                param = camera.getParameters();

                param.setPreviewSize(B.cameraView.getWidth(), B.cameraView.getHeight());
                camera.setParameters(param);

                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();*/
            }
            else {
                askCameraPermission();
            }
        }
        catch (Exception e) {
            Toast.makeText(this, "Error on surfaceCreated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "surfaceCreated: ", e);
        }
    }

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




    private boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }

        return PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED;
    }

    private void askCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        Toast.makeText(this, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openCamera();
                else {
                    Toast.makeText(this, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
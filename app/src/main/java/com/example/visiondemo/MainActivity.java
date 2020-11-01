package com.example.visiondemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.databinding.DataBindingUtil;

import com.example.visiondemo.databinding.ActivityMainBinding;
import com.icestorm.android.VisionCameraEventsListener;
import com.icestorm.android.mlkit.BitmapConverter;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements VisionCameraEventsListener {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;

    private ActivityMainBinding B;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = DataBindingUtil.setContentView(this, R.layout.activity_main);

        B.btnSwitchCamera.setOnClickListener(v -> B.cameraView.switchCamera());

        B.btnTakePickture.setOnClickListener(v -> {
            B.cameraView.captureImage();
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    B.cameraView.onCameraPermissionGranted();
                else {
                    Toast.makeText(this, "Bạn chưa cấp quyền Camera", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onCameraPictureTaken(String filePath) {
        Intent intent = new Intent(MainActivity.this, CapturedImageActivity.class);
        intent.putExtra("bitmap", filePath);
        startActivity(intent);
    }
}
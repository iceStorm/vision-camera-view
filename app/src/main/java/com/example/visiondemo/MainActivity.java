package com.example.visiondemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.icestorm.android.VisionCameraEventsListener;
import com.icestorm.android.VisionCameraView;


public class MainActivity extends AppCompatActivity implements VisionCameraEventsListener {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;
    private VisionCameraView cameraView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        cameraView = findViewById(R.id.cameraView);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case VisionCameraView.CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    cameraView.onCameraPermissionGranted();
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

    @Override
    public void onCameraUpdated(Bitmap bmp) {
        /*MlkitScanner.scanText(bmp, this);*/
    }


}
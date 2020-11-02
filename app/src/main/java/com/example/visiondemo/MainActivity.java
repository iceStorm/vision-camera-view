package com.example.visiondemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.visiondemo.databinding.ActivityMainBinding;
import com.google.mlkit.vision.text.Text;
import com.icestorm.android.VisionCameraEventsListener;


public class MainActivity extends AppCompatActivity implements VisionCameraEventsListener {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 0;
    private ActivityMainBinding B;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = DataBindingUtil.setContentView(this, R.layout.activity_main);
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

    @Override
    public void onBarcodeDetected(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTextDetected(Text textBlocks) {
        Log.i(TAG, "onTextDetected: " + textBlocks.getText());
    }

}
package com.example.visiondemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.visiondemo.databinding.ActivityCaturedImageBinding;

public class CapturedImageActivity extends AppCompatActivity {
    private static final String TAG = "CapturedImageActivity";
    private ActivityCaturedImageBinding B;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        B = DataBindingUtil.setContentView(this, R.layout.activity_catured_image);

        Intent receivedIntent = getIntent();
        String filePath = receivedIntent.getStringExtra("bitmap");

        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        B.imv.setImageBitmap(bmp);
    }
}
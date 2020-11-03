package com.example.visiondemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class CapturedImageActivity extends AppCompatActivity {
    private static final String TAG = "CapturedImageActivity";
    private ImageView imv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catured_image);
        imv = findViewById(R.id.imv);

        Intent receivedIntent = getIntent();
        String filePath = receivedIntent.getStringExtra("bitmap");

        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        imv.setImageBitmap(bmp);
    }
}
package com.icestorm.android.mlkit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapConverter {

    public static byte[] toByteArray(Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static Bitmap toBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}

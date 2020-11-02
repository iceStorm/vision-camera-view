package com.icestorm.android.utils;

import android.graphics.Bitmap;
import android.util.Pair;
import android.widget.ImageView;

public class ImageResizer {
    private Bitmap mSelectedImage;
    private ImageView mImageView;
    private Integer mImageMaxWidth;
    private Integer mImageMaxHeight;


    public ImageResizer(Bitmap mSelectedImage, ImageView mImageView) {
        this.mSelectedImage = mSelectedImage;
        this.mImageView = mImageView;
    }


    public Bitmap getResizedImage() {
        // Get the dimensions of the View
        Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

        int targetWidth = targetedSize.first;
        int maxHeight = targetedSize.second;

        // Determine how much to scale down the image
        float scaleFactor =
            Math.max(
                (float) mSelectedImage.getWidth() / (float) targetWidth,
                (float) mSelectedImage.getHeight() / (float) maxHeight);


        return Bitmap.createScaledBitmap(
            mSelectedImage,
            (int) (mSelectedImage.getWidth() / scaleFactor),
            (int) (mSelectedImage.getHeight() / scaleFactor),
            true);
    }


    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }


    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }


}

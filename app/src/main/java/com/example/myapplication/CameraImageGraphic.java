package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class CameraImageGraphic extends GraphicOverlay.Graphic {

    public final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, getTransformationMatrix(), null);
    }
}

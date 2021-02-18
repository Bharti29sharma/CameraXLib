package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class FaceGraphic  extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 8.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final int NUM_COLORS = 10;
    private static final int[][] COLORS =
            new int[][]{
                    // {Text color, background color}`
                    {Color.BLACK, Color.WHITE},
                    {Color.WHITE, Color.MAGENTA},
                    {Color.BLACK, Color.LTGRAY},
                    {Color.WHITE, Color.RED},
                    {Color.WHITE, Color.BLUE},
                    {Color.WHITE, Color.DKGRAY},
                    {Color.BLACK, Color.CYAN},
                    {Color.BLACK, Color.YELLOW},
                    {Color.WHITE, Color.BLACK},
                    {Color.BLACK, Color.GREEN}
            };

    private final Paint facePositionPaint, facePositionPaintBlack;
    private final Paint[] idPaints;
    private final Paint[] boxPaints;
    private final Paint[] labelPaints;

    private volatile Face face;
    Bitmap overlayBitmap, canvasBitmap;
    Drawable drawable;
    GraphicOverlay graphicOverlay;


    FaceGraphic(GraphicOverlay overlay, Face face) {
        super(overlay);
        graphicOverlay = overlay;


        this.face = face;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        facePositionPaintBlack = new Paint();
        facePositionPaintBlack.setColor(Color.BLACK);

        int numColors = COLORS.length;
        idPaints = new Paint[numColors];
        boxPaints = new Paint[numColors];
        labelPaints = new Paint[numColors];
        for (int i = 0; i < numColors; i++) {
            idPaints[i] = new Paint();
            idPaints[i].setColor(COLORS[i][0] /* text color */);
            idPaints[i].setTextSize(ID_TEXT_SIZE);

            boxPaints[i] = new Paint();
            boxPaints[i].setColor(COLORS[i][1] /* background color */);
            boxPaints[i].setStyle(Paint.Style.STROKE);
            boxPaints[i].setStrokeWidth(BOX_STROKE_WIDTH);

            labelPaints[i] = new Paint();
            labelPaints[i].setColor(COLORS[i][1] /* background color */);
            labelPaints[i].setStyle(Paint.Style.FILL);
        }

        //bitmap = Bitmap.createBitmap(500/*width*/, 500/*height*/, Bitmap.Config.ARGB_8888);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
       // canvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        //canvas = new Canvas(canvasBitmap);
        Face face = this.face;
        if (face == null) {
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());

        // Calculate positions.
        float left = x - scale(face.getBoundingBox().width() / 2.0f);
        float top = y - scale(face.getBoundingBox().height() / 2.0f);
        float right = x + scale(face.getBoundingBox().width() / 2.0f);
        float bottom = y + scale(face.getBoundingBox().height() / 2.0f);
        float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
        float yLabelOffset = (face.getTrackingId() == null) ? 0 : -lineHeight;

        // Decide color based on face ID
        int colorID = (face.getTrackingId() == null) ? 0 : Math.abs(face.getTrackingId() % NUM_COLORS);

        // Calculate width and height of label box
        float textWidth = idPaints[colorID].measureText("ID: " + face.getTrackingId());
        if (face.getSmilingProbability() != null) {
            yLabelOffset -= lineHeight;
            textWidth =
                    Math.max(
                            textWidth,
                            idPaints[colorID].measureText(
                                    String.format(Locale.US, "Happiness: %.2f", face.getSmilingProbability())));
        }
        if (face.getLeftEyeOpenProbability() != null) {
            yLabelOffset -= lineHeight;
            textWidth =
                    Math.max(
                            textWidth,
                            idPaints[colorID].measureText(
                                    String.format(
                                            Locale.US, "Left eye open: %.2f", face.getLeftEyeOpenProbability())));
        }
        if (face.getRightEyeOpenProbability() != null) {
            yLabelOffset -= lineHeight;
            textWidth =
                    Math.max(
                            textWidth,
                            idPaints[colorID].measureText(
                                    String.format(
                                            Locale.US, "Right eye open: %.2f", face.getRightEyeOpenProbability())));
        }

        yLabelOffset = yLabelOffset - 3 * lineHeight;
        textWidth =
                Math.max(
                        textWidth,
                        idPaints[colorID].measureText(
                                String.format(Locale.US, "EulerX: %.2f", face.getHeadEulerAngleX())));
        textWidth =
                Math.max(
                        textWidth,
                        idPaints[colorID].measureText(
                                String.format(Locale.US, "EulerY: %.2f", face.getHeadEulerAngleY())));
        textWidth =
                Math.max(
                        textWidth,
                        idPaints[colorID].measureText(
                                String.format(Locale.US, "EulerZ: %.2f", face.getHeadEulerAngleZ())));
        // Draw labels
        canvas.drawRect(
                left - BOX_STROKE_WIDTH,
                top + yLabelOffset,
                left + textWidth + (2 * BOX_STROKE_WIDTH),
                top,
                labelPaints[colorID]);
        yLabelOffset += ID_TEXT_SIZE;
        canvas.drawRect(left, top, right, bottom, boxPaints[colorID]);
        if (face.getTrackingId() != null) {
            canvas.drawText("ID: " + face.getTrackingId(), left, top + yLabelOffset, idPaints[colorID]);
            yLabelOffset += lineHeight;
        }


        // Draws all face contours.
        for (FaceContour contour : face.getAllContours()) {
            for (PointF point : contour.getPoints()) {
                canvas.drawCircle(
                        translateX(point.x), translateY(point.y), FACE_POSITION_RADIUS, facePositionPaint);
            }
        }

        try {

            if (face.getAllLandmarks() != null) {
                float cheek1Left = translateX(face.getLandmark(FaceLandmark.LEFT_EAR).getPosition().x);
                float cheek1Right = translateX(face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().x);
                float cheek1Top = translateY(face.getLandmark(FaceLandmark.LEFT_EYE).getPosition().y);
                float cheek1Bottom = translateY(face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().y);

                float cheek2Left = translateX(face.getLandmark(FaceLandmark.RIGHT_EAR).getPosition().x);
                float cheek2Right = translateX(face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().x);
                float cheek2Top = translateY(face.getLandmark(FaceLandmark.RIGHT_EYE).getPosition().y);
                float cheek2Bottom = translateY(face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().y);


                canvas.drawRect(cheek1Left, cheek1Top + 50, cheek1Right + 60,
                        cheek1Bottom, boxPaints[1]);

                canvas.drawRect(cheek2Left - 20, cheek2Top + 50, cheek2Right - 60,
                        cheek2Bottom, boxPaints[1]);

                //overlayBitmap.setPixels(cheek2Left - 20, cheek2Bottom ,Color.GREEN);
                Rect rect = new Rect((int) cheek2Left, (int) cheek2Top, (int) cheek2Right, (int) cheek2Bottom);
                overlayBitmap = ((CameraImageGraphic) graphicOverlay.graphics.get(0)).bitmap;
                // croppedImg =   cropBitmap(overlayBitmap,face.getBoundingBox());


                overlayBitmap = getCroppedBitmap(overlayBitmap);
                if (overlayBitmap != null)
                    //if(createTransparentBitmapFromBitmap(overlayBitmap ,Color.BLACK) != null )
                    MediaStore.Images.Media.insertImage(this.getApplicationContext().getContentResolver(), overlayBitmap, "CroppedFace", "FaceDetaction");


                Log.d("face.getAllContours()", face.getAllContours().toString());

                Log.d("face.getAllLandmarks()", face.getAllLandmarks().toString());

                Log.d("face.getBoundingBox()", face.getBoundingBox().toString());




            }
        } catch (Exception e) {
            Log.e("Cropp face error", e.getMessage().toString());
        }


        // Draws smiling and left/right eye open probabilities.
        if (face.getSmilingProbability() != null) {
            canvas.drawText(
                    "Smiling: " + String.format(Locale.US, "%.2f", face.getSmilingProbability()),
                    left,
                    top + yLabelOffset,
                    idPaints[colorID]);
            yLabelOffset += lineHeight;
        }

//        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
//        if (face.getLeftEyeOpenProbability() != null) {
//            canvas.drawText(
//                    "Left eye open: " + String.format(Locale.US, "%.2f", face.getLeftEyeOpenProbability()),
//                    left,
//                    top + yLabelOffset,
//                    idPaints[colorID]);
//            yLabelOffset += lineHeight;
//        }
//        if (leftEye != null) {
//            float leftEyeLeft =
//                    translateX(leftEye.getPosition().x) - idPaints[colorID].measureText("Left Eye") / 2.0f;
//            canvas.drawRect(
//                    leftEyeLeft - BOX_STROKE_WIDTH,
//                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
//                    leftEyeLeft + idPaints[colorID].measureText("Left Eye") + BOX_STROKE_WIDTH,
//                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
//                    labelPaints[colorID]);
//            canvas.drawText(
//                    "Left Eye",
//                    leftEyeLeft,
//                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET,
//                    idPaints[colorID]);
//        }
//
//        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
//        if (face.getRightEyeOpenProbability() != null) {
//            canvas.drawText(
//                    "Right eye open: " + String.format(Locale.US, "%.2f", face.getRightEyeOpenProbability()),
//                    left,
//                    top + yLabelOffset,
//                    idPaints[colorID]);
//        }
//        if (rightEye != null) {
//            float rightEyeLeft =
//                    translateX(rightEye.getPosition().x) - idPaints[colorID].measureText("Right Eye") / 2.0f;
//            canvas.drawRect(
//                    rightEyeLeft - BOX_STROKE_WIDTH,
//                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
//                    rightEyeLeft + idPaints[colorID].measureText("Right Eye") + BOX_STROKE_WIDTH,
//                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
//                    labelPaints[colorID]);
//            canvas.drawText(
//                    "Right Eye",
//                    rightEyeLeft,
//                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET,
//                    idPaints[colorID]);
//            yLabelOffset += lineHeight;
//        }

        canvas.drawText(
                "EulerX: " + face.getHeadEulerAngleX(), left, top + yLabelOffset, idPaints[colorID]);
        yLabelOffset += lineHeight;
        canvas.drawText(
                "EulerY: " + face.getHeadEulerAngleY(), left, top + yLabelOffset, idPaints[colorID]);
        yLabelOffset += lineHeight;
        canvas.drawText(
                "EulerZ: " + face.getHeadEulerAngleZ(), left, top + yLabelOffset, idPaints[colorID]);

        // Draw facial landmarks
        drawFaceLandmark(canvas, FaceLandmark.LEFT_EYE);
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_EYE);
        drawFaceLandmark(canvas, FaceLandmark.LEFT_CHEEK);
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_CHEEK);
        drawFaceLandmark(canvas, FaceLandmark.MOUTH_BOTTOM);
        drawFaceLandmark(canvas, FaceLandmark.MOUTH_LEFT);
        drawFaceLandmark(canvas, FaceLandmark.MOUTH_RIGHT);
    }

    private void drawFaceLandmark(Canvas canvas, @FaceLandmark.LandmarkType int landmarkType) {
        FaceLandmark faceLandmark = face.getLandmark(landmarkType);
        if (faceLandmark != null) {
            canvas.drawCircle(
                    translateX(faceLandmark.getPosition().x),
                    translateY(faceLandmark.getPosition().y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);


        }


    }

    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        final Paint boxPaint;
        //  boxPaint = new Paint[5];
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN /* background color */);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
        int w = rect.right - rect.left;
        int h = rect.bottom - rect.top;
        Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
        // Canvas canvas = new Canvas(ret);
        // canvas.drawBitmap(bitmap, -rect.left, -rect.top,boxPaint);

        return ret;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}












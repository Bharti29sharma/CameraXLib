package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.myapplication.model.HealthData;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            boxPaints[i].setFilterBitmap(true);

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

        Face face = this.face;
        if (face == null) {
            return;
        }


        overlayBitmap = ((CameraImageGraphic) graphicOverlay.graphics.get(0)).bitmap;

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());

        // Calculate positions.
        float left = x - scale(face.getBoundingBox().width() / 2.0f);
        float top = y - scale(face.getBoundingBox().height() / 2.0f);
        float right = x + scale(face.getBoundingBox().width() / 2.0f);
        float bottom = y + scale(face.getBoundingBox().height() / 2.0f);

        // Decide color based on face ID
        int colorID = (face.getTrackingId() == null) ? 0 : Math.abs(face.getTrackingId() % NUM_COLORS);

        float h_delta =(float) 0.1 * face.getBoundingBox().width();
        float  v_delta =(float) 0.1 * face.getBoundingBox().height();
        canvas.drawLine(left, top, left, top+v_delta, boxPaints[colorID]);
        canvas.drawLine(left, top, left+h_delta, top, boxPaints[colorID]);

        canvas.drawLine(left, bottom, left, bottom-v_delta, boxPaints[colorID]);
        canvas.drawLine(left, bottom, left+h_delta, bottom, boxPaints[colorID]);

        canvas.drawLine(right, top, right-h_delta, top, boxPaints[colorID]);
        canvas.drawLine(right, top, right, top+v_delta, boxPaints[colorID]);

        canvas.drawLine(right, bottom, right-h_delta, bottom, boxPaints[colorID]);
        canvas.drawLine(right, bottom, right, bottom-v_delta, boxPaints[colorID]);





//        // Draws all face contours.
//        for (FaceContour contour : face.getAllContours()) {
//
//            for (PointF point : contour.getPoints()) {
//                canvas.drawCircle(
//                        translateX(point.x), translateY(point.y), FACE_POSITION_RADIUS, facePositionPaint);
//
//            }
//        }

        if(SingletonClass.getInstance().isRecordingStarted == true && SingletonClass.getInstance().isRecordingFinished == false) {
            try {
                SingletonClass.getInstance().frameCount ++;
                drawShapesOnBitmap();

                if(SingletonClass.getInstance().frameCount == 30){
                    SingletonClass.getInstance().isRecordingFinished = true;
                }
               // if verlayBitmap != null)
                 //   MediaStore.Images.Media.insertImage(this.getApplicationContext().getContentResolver(), overlayBitmap, "CroppedFace", "FaceDetaction");



               // Log.d("face.getAllContours()", face.getAllContours().toString());


            } catch (Exception e) {
                Log.e("Cropp face error", e.getMessage().toString());
            }
        }
        else if(SingletonClass.getInstance().isRecordingFinished == true){
            //REST api call and finish activity
        int size =    SingletonClass.getInstance().leftcheekFramMap.size();
            Log.v("Frame Count :: ", String.valueOf(SingletonClass.getInstance().frameCount));

            getDataFromServer();
        }


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

    private void  drawShapesOnBitmap(){

        HashMap leftCheekMap , rightCheekMap ,foreheadMap , noseMap;

        FaceLandmark leftEyefaceLandmark = face.getLandmark(FaceLandmark.LEFT_EYE);
        FaceLandmark rightEyefaceLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE);
        PointF noseTopPoint ,noseLeftPoint, noseRightPoint,noseBottomPoint ,foreheadTopPoint ;
        foreheadTopPoint = new PointF();
        noseTopPoint = new PointF();
        noseLeftPoint = new PointF();
        noseRightPoint = new PointF();
        noseBottomPoint = new PointF();

        for (FaceContour contour : face.getAllContours()) {
            if(contour.getFaceContourType() == 1) {
                foreheadTopPoint = contour.getPoints().get(0);
            }else if(contour.getFaceContourType() == 12){
                noseTopPoint =  contour.getPoints().get(0);
                noseBottomPoint = contour.getPoints().get(1);
            }else if(contour.getFaceContourType() == 13){
                noseLeftPoint = contour.getPoints().get(2);
                noseRightPoint = contour.getPoints().get(0);
            }


        }
        // Draw Forehead patch
        int forehead_xLeft  = (int) leftEyefaceLandmark.getPosition().x;
        int forehead_xRight = (int) rightEyefaceLandmark.getPosition().x;
        int forehead_yTop   = (int) foreheadTopPoint.y+10;
        int forehead_yBottom  = (int)  leftEyefaceLandmark.getPosition().y-25 ;



        int foreheadArrayLength =    (forehead_xRight-forehead_xLeft) * (forehead_yBottom - forehead_yTop);  // width * height
        int[] foreheadPixelArray = new int[foreheadArrayLength];
        int  foreheadArrayIndex=0;


        for( int i = forehead_yTop; i<=forehead_yBottom ; i++){

            for(int j = forehead_xLeft ; j <=  forehead_xRight ; j++){


                if(foreheadArrayIndex < foreheadArrayLength) {
                    foreheadPixelArray[foreheadArrayIndex] = overlayBitmap.getPixel(j,i);
                    foreheadArrayIndex++;
                }
               // overlayBitmap.setPixel(j,i, Color.GREEN);

            }
        }

     foreheadMap =    getRGBValue(foreheadPixelArray);

        //Draw Nose Patch



       int noseXRight =  (int)noseRightPoint.x;
        int noseXLeft =  (int)noseLeftPoint.x;

        int nosePixArrayLength =    ( ((int)noseXLeft+5)-( (int)noseXRight-5)) * ((int)noseBottomPoint.y - (int)noseTopPoint.y);  // width * height
        int[] nosePixelArray = new int[nosePixArrayLength];
        int noseArrayIndex=0;

        for( int i =(int) noseBottomPoint.y; i>= (int)noseTopPoint.y ; i--){

            for(int j = noseXRight-5 ; j <=  noseXLeft+5 ; j++){


                if(noseArrayIndex < nosePixArrayLength) {
                    nosePixelArray[noseArrayIndex] = overlayBitmap.getPixel(j, i);
                    noseArrayIndex++;
                }

              //  overlayBitmap.setPixel(j,i, Color.GREEN);

            }

            noseXRight ++;
            noseXLeft--;

        }

       noseMap = getRGBValue(nosePixelArray);

// Draw right Cheek Patch
        int iLength = (int)face.getLandmark(FaceLandmark.RIGHT_EAR).getPosition().x-10;
        int i = (int) face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().x+20;
        int j =(int) face.getLandmark(FaceLandmark.RIGHT_EYE).getPosition().y+10;
        int jLength = (int) face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().y;


        int arrayLength =    (iLength-i) * (jLength - j);  // width * height
        int[] pixelArray = new int[arrayLength];
        int arrayIndex=0;



        for (int l = j; l <= jLength; l++) {
            for (int k = i; k <= iLength; k++) {

                if(arrayIndex < arrayLength) {
                    pixelArray[arrayIndex] = overlayBitmap.getPixel(k, l);
                    arrayIndex++;
                }
               // overlayBitmap.setPixel(k,l, Color.GREEN);

            }
        }

     rightCheekMap =   getRGBValue(pixelArray);

        // Draw left cheek patch

        int leftCheek_xRight = (int)face.getLandmark(FaceLandmark.LEFT_EAR).getPosition().x+10;
        int leftCheek_xLeft = (int) face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().x-20;
        int leftCheek_yBottom =(int) face.getLandmark(FaceLandmark.LEFT_EYE).getPosition().y+10;
        int leftCheek_yTop = (int) face.getLandmark(FaceLandmark.NOSE_BASE).getPosition().y;

        int leftCheekHeight = leftCheek_yTop-leftCheek_yBottom;
        int leftCheekWidth = leftCheek_xLeft - leftCheek_xRight;

        int leftCheekPixArrayLength =    leftCheekWidth * leftCheekHeight;  // width * height
        int[] leftCheekPixelArray = new int[leftCheekPixArrayLength];
        int leftCheekArrayIndex=0;

        for (int l  =leftCheek_yBottom ; l <= leftCheek_yTop; l++) {
            for (int k = leftCheek_xRight; k <= leftCheek_xLeft; k++) {

                if(leftCheekArrayIndex < leftCheekPixArrayLength) {
                    leftCheekPixelArray[leftCheekArrayIndex] = overlayBitmap.getPixel(k, l);
                    leftCheekArrayIndex++;
                }

               // overlayBitmap.setPixel(k,l, Color.GREEN);

            }
        }

        leftCheekMap =  getRGBValue(leftCheekPixelArray);



        // Get 1D array of RGB for left cheek

        int[] calculatedRGB  = new int[leftCheekMap.size()];
        int index =0;
            for (Object key : leftCheekMap.keySet()) {

                calculatedRGB[index] = (int) leftCheekMap.get(key);
                index ++;

         }



        SingletonClass.getInstance().leftcheekFramMap.put(SingletonClass.getInstance().frameCount,  calculatedRGB);

//        for (Object key :  SingletonClass.getInstance().leftcheekFramMap.keySet()) {
//
//            calculatedRGB[index] = (int) leftCheekMap.get(key);
//            index ++;
//
//        }

       // if(SingletonClass.getInstance().frameCount == 30)
       // if(SingletonClass.getInstance().isRecordingFinished)
       // getDataFromServer();
    }


    public HashMap getRGBValue(int[] pixels){

        HashMap map = new HashMap();



        for(int i =0 ; i < pixels.length ; i ++ ){


          int  red = (pixels[i]) >> 16 & 0xff;
          int  green   = (pixels[i]) >> 8 & 0xff;
          int  blue  = (pixels[i]) & 0xff;

            map.put(pixels[i] , red + green + blue);
           // if(i<10)
           // Log.v("HashMap of RGB value" , map.toString());


        }

        return map;



    }

    // void  getDataFromServer (int[] value_array,int width ,int height , int n_channels){


    void  getDataFromServer (){
     APIInterface   apiInterface = APIClient.getClient().create(APIInterface.class);



    //  Call<HealthData> call = apiInterface.getHealthData(value_array,width,height,n_channels);
      Call<HealthData> call = apiInterface.getHealthData();
      call.enqueue(new Callback<HealthData>() {
          @Override
          public void onResponse(Call<HealthData> call, Response<HealthData> response) {


              Log.d("response TAG",response.code()+"");
              SingletonClass.getInstance().restResponse = response;

          }

          @Override
          public void onFailure(Call<HealthData> call, Throwable t) {
              call.cancel();
          }
      });

    }


}












package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Html;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public final class MainActivity extends  AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {


    private static final String TAG = "AndroidCameraApi";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    String selectedModel = "Face Detection";
    private int progressStatus = 0;
    Handler handler =new Handler();
    long timeRemaining =0;
    int previousProgress = 0;


    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.preview_view);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }

        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
       // ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        ImageButton recordingButton = findViewById(R.id.record_button);

        ProgressBar progressBar = findViewById(R.id.progressBar);

      //  getActionBar().setBackgroundDrawable(getDrawable(R.drawable.remedic_mini));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.remedic_mini_wo_bg);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        String centerNBlack = "<div style='text-align:center' ><span style='color:black' >REMEDIC</span></div>";
        getSupportActionBar().setTitle(Html.fromHtml(centerNBlack));



        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SingletonClass.getInstance().isRecordingStarted  = true;
               // recordingButton.setEnabled(false);

                if(recordingButton.getTag().equals("Record")) {
                    //Start recording
                    recordingButton.setImageResource(R.drawable.ic_stop);
                    recordingButton.setTag("Stop");

                  //  cameraSource.stop();
                    //startCameraSource();
                }
                else if(recordingButton.getTag().equals("Stop")){
                    //stop recording
                    recordingButton.setImageResource(R.drawable.ic_play_button);
                    recordingButton.setTag("Play");
                    cameraSource.stop();

                }
                else if(recordingButton.getTag().equals("Play")){
                    //Restart recording
                    recordingButton.setImageResource(R.drawable.ic_stop);
                    recordingButton.setTag("Stop");

                    try {

                        cameraSource.start();
                    } catch (IOException e) {
                        Log.e("Start preview ", e.getMessage().toString());
                        e.printStackTrace();
                    }
                }
                progressBar.setVisibility(View.VISIBLE);



                if(!recordingButton.getTag().equals("Play"))
                new Thread(new Runnable() {
                    public void run() {

                        progressStatus = previousProgress;
                        while (progressStatus < 3000) {
                            progressStatus += 1;
                            // Update the progress bar and display the
                            //current value in the text view
                            handler.post(new Runnable() {
                                public void run() {
                                    if(recordingButton.getTag().equals("Play")) {
                                        previousProgress = progressStatus;
                                        progressStatus = 3000;
                                       // handler.removeCallbacks(null);

                                    }
                                    else {
                                        progressBar.setProgress(progressStatus);
                                        if(progressStatus==progressBar.getMax()){
                                            SingletonClass.getInstance().isRecordingFinished  = true;
                                            Intent intent = new Intent(getApplicationContext(), UploadingActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                }
                            });
                            try {
                                // Sleep for 200 milliseconds.
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();






                ///////////////////////////
//                if (recordingButton.getTag().equals("Stop")) {
//                    long milisInFuture;
//                    int progress;
//                    if(timeRemaining == 0) {
//                        milisInFuture = 6000;
//
//                    }
//                    else
//                        milisInFuture = timeRemaining;
//                   // progress = previousProgress;
//
//
//                    long countDownInterval = milisInFuture/10;
//
//                    new CountDownTimer(milisInFuture, countDownInterval) {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
//                                //this will be done every 1000 milliseconds ( 1 seconds )
//                            int progress = (int) ((milisInFuture - millisUntilFinished) / countDownInterval);
//                            if(recordingButton.getTag().equals("Play")){
//                                timeRemaining =milisInFuture;
//                                previousProgress= progress;
//                                this.cancel();
//
//                            }
//                            else {
//
//                                if(milisInFuture == timeRemaining)
//                                    progress = progress+ previousProgress;
//                                progressBar.setProgress(progress);
//                            }
//
//
//                        }
//
//                        @Override
//                        public void onFinish() {
//
////                        SingletonClass.getInstance().isRecordingFinished  = true;
////                           Intent intent = new Intent(getApplicationContext(), UploadingActivity.class);
////                           startActivity(intent);
////                           finish();
//                        }
//
//                    }.start();
//                }
//




            }
        });



        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }
        if (cameraSource != null) {
            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
        }

    }
       // facingSwitch.setOnCheckedChangeListener(this);


//        @Override
//        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            Log.d(TAG, "Set facing");
//            if (cameraSource != null) {
//                if (isChecked) {
//                    cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
//                } else {
//                    cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
//                }
//            }
//            preview.stop();
//            startCameraSource();
//        }

        private void createCameraSource (String model){
            // If there's no existing cameraSource, create one.
            if (cameraSource == null) {
                cameraSource = new CameraSource(this, graphicOverlay);
            }

            Log.i(TAG, "Using Face Detector Processor");
            FaceDetectorOptions faceDetectorOptions =
                    PreferenceUtils.getFaceDetectorOptionsForLivePreview(this);
            cameraSource.setMachineLearningFrameProcessor(
                    new FaceDetectorProcessor(this, faceDetectorOptions));


        }

        private void startCameraSource () {
            if (cameraSource != null) {
                try {
                    if (preview == null) {
                        Log.d(TAG, "resume: Preview is null");
                    }
                    if (graphicOverlay == null) {
                        Log.d(TAG, "resume: graphOverlay is null");
                    }
                    preview.start(cameraSource, graphicOverlay);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source.", e);
                    cameraSource.release();
                    cameraSource = null;
                }
            }
               }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem toggleservice = menu.findItem(R.id.menu_toogle);
        final Switch actionView = (Switch) toggleservice.getActionView();
        actionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Start or stop your Service
                if (cameraSource != null) {
                    if ( buttonView.isChecked()) {
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);

                    } else {
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                    }
                }
                preview.stop();
                startCameraSource();
            }
        });
        return super.onCreateOptionsMenu(menu);
        //return true;
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
//        case R.id.menu_toogle:
//            Log.d(TAG, "Set facing");
//
//            if (cameraSource != null) {
//                if ( item.isChecked()) {
//                    cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
//                } else {
//                    cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
//                }
//            }
//            preview.stop();
//            startCameraSource();
//            return(true);
//
//    }
//        return(super.onOptionsItemSelected(item));
//    }


    @Override
        public void onResume () {
            super.onResume();
            Log.d(TAG, "onResume");
            createCameraSource(selectedModel);
            startCameraSource();
        }

        /** Stops the camera. */
        @Override
        protected void onPause () {
            super.onPause();
            preview.stop();
        }

        @Override
        public void onDestroy () {
            super.onDestroy();
            if (cameraSource != null) {
                cameraSource.release();
            }
        }

        private String[] getRequiredPermissions () {
            try {
                PackageInfo info =
                        this.getPackageManager()
                                .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
                String[] ps = info.requestedPermissions;
                if (ps != null && ps.length > 0) {
                    return ps;
                } else {
                    return new String[0];
                }
            } catch (Exception e) {
                return new String[0];
            }
        }

        private boolean allPermissionsGranted () {
            for (String permission : getRequiredPermissions()) {
                if (!isPermissionGranted(this, permission)) {
                    return false;
                }
            }
            return true;
        }

        private void getRuntimePermissions () {
            List<String> allNeededPermissions = new ArrayList<>();
            for (String permission : getRequiredPermissions()) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission);
                }
            }

            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(
                        this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
            }
        }

        @Override
        public void onRequestPermissionsResult (
        int requestCode, String[] permissions,int[] grantResults){

            Log.i(TAG, "Permission granted!");
            if (allPermissionsGranted()) {
                createCameraSource(selectedModel);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        private static boolean isPermissionGranted(Context context, String permission){
            if (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: " + permission);
                return true;
            }
            Log.i(TAG, "Permission NOT granted: " + permission);
            return false;
        }


}
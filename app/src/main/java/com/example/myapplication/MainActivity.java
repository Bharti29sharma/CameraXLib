package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public final class MainActivity extends  AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "AndroidCameraApi";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }

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
        ToggleButton facingSwitch = findViewById(R.id.facing_switch);


        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
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
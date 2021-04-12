package com.example.dbr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.TextResult;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {

    private CameraDevice opened_camera;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private Timer timer;
    private Surface texture_surface;
    private CaptureRequest.Builder requestBuilder;
    private BarcodeReader dbr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initImageReader();
        try {
            dbr = new BarcodeReader("t0077xQAAAEoQXMjVnF7S9ar4W6em9rhE6UN4uhNa+YU3O8VoTOiYEG2LOvx/G5HZYmRRsWXXHDMr+z0wUHfFh1aBqBJJZ3z1KUd/ACB1Kag=");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        if (checkPermission()==true){
            TextureView textureView = findViewById(R.id.textureView);
            TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    texture_surface = new Surface(textureView.getSurfaceTexture());
                    openCamera(texture_surface);
                }

            };
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
        timer = new Timer();
        class DecodeFrame extends TimerTask {
            public void run() {
                refocus();
                capture();
            }
        }
        TimerTask decodeFrame = new DecodeFrame();
        timer.scheduleAtFixedRate(decodeFrame, 2000, 2000);
    }

    private void refocus(){
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(requestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            cameraCaptureSession.capture(requestBuilder.build(), null,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void OkayButton_Clicked(View view){
        capture();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera(Surface texture_surface) {

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        CameraDevice.StateCallback cam_stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {

                opened_camera = camera;
                try {

                    requestBuilder = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    requestBuilder.addTarget(texture_surface);

                    CaptureRequest request = requestBuilder.build();

                    CameraCaptureSession.StateCallback cam_capture_session_stateCallback = new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            try {
                                session.setRepeatingRequest(request, null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    };

                    opened_camera.createCaptureSession(Arrays.asList(texture_surface,imageReader.getSurface()), cam_capture_session_stateCallback, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }

        };

        checkPermission();

        try {
            cameraManager.openCamera(cameraManager.getCameraIdList()[0],cam_stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean checkPermission() {
        Boolean granted = false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
            Intent data = new Intent();
            data.putExtra("needRestart",true);
            setResult(20, data);
            finish();
        } else{
            granted=true;
        }
        return granted;
    }

    private void capture(){
        CaptureRequest.Builder requestBuilder_image_reader = null;
        try {
            requestBuilder_image_reader = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        requestBuilder_image_reader.set(CaptureRequest.JPEG_ORIENTATION,90);
        requestBuilder_image_reader.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

        Surface imageReaderSurface = imageReader.getSurface();
        requestBuilder_image_reader.addTarget(imageReaderSurface);
        try {
            cameraCaptureSession.capture(requestBuilder_image_reader.build(),null,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initImageReader(){
        imageReader = ImageReader.newInstance(1000, 1000, ImageFormat.JPEG, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                DecodingThread decodeThread = new DecodingThread(reader);
                decodeThread.run();
            }
        },null);
    }

    class DecodingThread implements Runnable {
        private ImageReader reader;
        public DecodingThread (ImageReader reader)
        {
            this.reader = reader;
        }
        @Override
        public void run() {
            Image image= reader.acquireLatestImage();
            ByteBuffer buffer= image.getPlanes()[0].getBuffer();
            int length= buffer.remaining();
            byte[] bytes= new byte[length];
            buffer.get(bytes);
            image.close();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, length);

            TextResult[] results = new TextResult[0];
            try {
                results = dbr.decodeBufferedImage(bitmap,"");
            } catch (BarcodeReaderException | IOException e) {
                e.printStackTrace();
            }

            // e.g. TextResult[] results = dbr.decodeFile("/storage/dbr-preview-img/test.jpg", "");
            //Toast.makeText(this, results.length , Toast.LENGTH_LONG).show();
            if (results.length > 0) {
                String resultContent = "Found " + results.length + " barcode(s):\n";
                for (int i = 0; i < results.length; i++) {
                    resultContent += results[i].barcodeText + "\n";
                }
                Log.d("DBR", resultContent);
                Intent data = new Intent();
                data.putExtra("TextResult",resultContent);
                setResult(20, data);
                opened_camera.close();
                timer.cancel();
                finish();
            } else {
                Log.d("DBR", "No barcode found");
            }
        }
    }
}
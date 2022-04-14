package com.dynamsoft.cameraxminimum;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRLicenseVerificationListener;
import com.dynamsoft.dbr.EnumImagePixelFormat;
import com.dynamsoft.dbr.TextResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView resultView;
    private ExecutorService exec;
    private Camera camera;
    private BarcodeReader dbr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);
        resultView = findViewById(R.id.resultView);
        initDBR();
        exec = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreviewAndImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void initDBR(){
        BarcodeReader.initLicense("DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9", new DBRLicenseVerificationListener() {
            @Override
            public void DBRLicenseVerificationCallback(boolean isSuccessful, Exception e) {
                if (!isSuccessful) {
                    e.printStackTrace();
                }
            }
        });
        try {
            dbr = new BarcodeReader();
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("UnsafeExperimentalUsageError")
    private void bindPreviewAndImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {

        int orientation = getApplicationContext().getResources().getConfiguration().orientation;
        Size resolution;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            resolution = new Size(720, 1280);
        }else{
            resolution = new Size(1280, 720);
        }

        Preview.Builder previewBuilder = new Preview.Builder();
        previewBuilder.setTargetResolution(resolution);
        Preview preview = previewBuilder.build();

        ImageAnalysis.Builder imageAnalysisBuilder=new ImageAnalysis.Builder();

        //invert
        //Camera2Interop.Extender ext = new Camera2Interop.Extender<>(imageAnalysisBuilder);
        //ext.setCaptureRequestOption(CaptureRequest.CONTROL_EFFECT_MODE,CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE);

        imageAnalysisBuilder.setTargetResolution(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);

        ImageAnalysis imageAnalysis = imageAnalysisBuilder.build();

        imageAnalysis.setAnalyzer(exec, new ImageAnalysis.Analyzer() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees =image.getImageInfo().getRotationDegrees();
                TextResult[] results = null;
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                int nRowStride = image.getPlanes()[0].getRowStride();
                int nPixelStride = image.getPlanes()[0].getPixelStride();
                int length= buffer.remaining();
                byte[] bytes= new byte[length];
                buffer.get(bytes);
                ImageData imageData= new ImageData(bytes,image.getWidth(), image.getHeight(),nRowStride *nPixelStride);
                try {
                    results = dbr.decodeBuffer(imageData.mBytes,imageData.mWidth,imageData.mHeight, imageData.mStride, EnumImagePixelFormat.IPF_NV21);
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Found ").append(results.length).append(" barcode(s):\n");
                for (int i = 0; i < results.length; i++) {
                    sb.append(results[i].barcodeText);
                    sb.append("\n");
                }
                Log.d("DBR", sb.toString());
                runOnUiThread(()->{resultView.setText(sb.toString());});
                image.close();
            }
        });

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .build();
        camera=cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, useCaseGroup);
    }

    private class ImageData{
        private int mWidth,mHeight,mStride;
        byte[] mBytes;
        ImageData(byte[] bytes ,int nWidth,int nHeight,int nStride){
            mBytes = bytes;
            mWidth = nWidth;
            mHeight = nHeight;
            mStride = nStride;
        }
    }
}

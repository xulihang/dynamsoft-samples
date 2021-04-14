package com.dynamsoft.camerax;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.CaptureConfig;
import androidx.camera.core.impl.SessionConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumImagePixelFormat;
import com.dynamsoft.dbr.TextResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView textView;
    private ImageView imageView;
    private BarcodeReader dbr;
    private ExecutorService exec;
    private Camera camera;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        exec = Executors.newSingleThreadExecutor();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            dbr = new BarcodeReader("t0077xQAAAEoQXMjVnF7S9ar4W6em9rhE6UN4uhNa+YU3O8VoTOiYEG2LOvx/G5HZYmRRsWXXHDMr+z0wUHfFh1aBqBJJZ3z1KUd/ACB1Kag=");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        textView = findViewById(R.id.resultView);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onBackPressed() {
        if (imageView.getVisibility()==View.VISIBLE){
            imageView.setVisibility(View.INVISIBLE);
        }else{
            super.onBackPressed();
        }

    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(exec, new ImageAnalysis.Analyzer() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void analyze(@NonNull ImageProxy image) {
                if (imageView.getVisibility()==View.VISIBLE && prefs.getBoolean("continuous",false)==false){ //non-continuous, scanned
                    image.close();
                    return;
                }
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                int nRowStride = image.getPlanes()[0].getRowStride();
                int nPixelStride = image.getPlanes()[0].getPixelStride();
                int length= buffer.remaining();
                byte[] bytes= new byte[length];
                buffer.get(bytes);
                ImageData imageData= new ImageData(bytes,image.getWidth(), image.getHeight(),nRowStride *nPixelStride);
                TextResult[] results = new TextResult[0];
                try {
                    results = dbr.decodeBuffer(imageData.mBytes,imageData.mWidth,imageData.mHeight, imageData.mStride, EnumImagePixelFormat.IPF_NV21,"");
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }
                showResult(previewView.getBitmap(),results);
                image.close();
            }
        });

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        camera=cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,
                imageAnalysis, preview);
    }

    private void showResult(Bitmap image,TextResult[] results){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (results.length > 0) {
                    String resultContent = "Found " + results.length + " barcode(s):\n";
                    for (int i = 0; i < results.length; i++) {
                        resultContent += results[i].barcodeText + "\n";
                    }
                    Log.d("DBR", resultContent);
                    textView.setText(resultContent);
                    Boolean continuous = prefs.getBoolean("continuous",false);
                    Log.d("DBR", String.valueOf(continuous));
                    if (continuous==false){
                        imageView.setImageBitmap(image);
                        imageView.setVisibility(View.VISIBLE);
                    }else{
                        imageView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Log.d("DBR", "No barcode found");
                    textView.setText("");
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private Bitmap imageProxyToBitmap(ImageProxy image) throws IOException {
        @SuppressLint("UnsafeExperimentalUsageError")
        Image.Plane[] planes = image.getImage().getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
        //saveImage(out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void saveImage(ByteArrayOutputStream out) throws IOException {
        File path = this.getExternalFilesDir(null);
        File file = new File(path, "test.jpg");
        Log.d("DBR", file.getAbsolutePath());
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(out.toByteArray());
        outStream.close();
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


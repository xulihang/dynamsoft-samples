package com.dynamsoft.camerax;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumImagePixelFormat;
import com.dynamsoft.dbr.EnumIntermediateResultSavingMode;
import com.dynamsoft.dbr.EnumIntermediateResultType;
import com.dynamsoft.dbr.Point;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
            PublicRuntimeSettings rs = dbr.getRuntimeSettings();
            rs.intermediateResultSavingMode= EnumIntermediateResultSavingMode.IRSM_MEMORY;
            rs.intermediateResultTypes= EnumIntermediateResultType.IRT_ORIGINAL_IMAGE;
            dbr.updateRuntimeSettings(rs);
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        textView = findViewById(R.id.resultView);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        SeekBar zoomRatioSeekBar = findViewById(R.id.zoomRatioSeekBar);
        zoomRatioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    camera.getCameraControl().setLinearZoom((float) progress/100);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

    public void torchToggled(View view){
        ToggleButton btn= (ToggleButton) view;
        camera.getCameraControl().enableTorch(btn.isChecked());
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(exec, new ImageAnalysis.Analyzer() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void analyze(@NonNull ImageProxy image) {
                if (imageView.getVisibility()==View.VISIBLE && prefs.getBoolean("continuous",false)==false){ //non-continuous, scanned
                    image.close();
                    return;
                }
                Image im = image.getImage();
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
                if (results.length>0){
                    YuvToRgbConverter converter = new YuvToRgbConverter(CameraActivity.this);
                    Bitmap bitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(), Bitmap.Config.ARGB_8888);
                    converter.yuvToRgb(image.getImage(),bitmap);
                    showResult(bitmap,results);
                } else{
                    showResult(null,results);
                }

                image.close();
            }
        });

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .build();

        camera=cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, useCaseGroup);
    }

    private void showResult(Bitmap bitmap,TextResult[] results){
        this.runOnUiThread(new Runnable() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void run() {
                if (results.length > 0) {
                    overlay(results,bitmap);
                    String resultContent = "Found " + results.length + " barcode(s):\n";
                    for (int i = 0; i < results.length; i++) {
                        resultContent += results[i].barcodeText + "\n";
                    }
                    Log.d("DBR", resultContent);
                    textView.setText(resultContent);
                    Boolean continuous = prefs.getBoolean("continuous",false);
                    Boolean record_history = prefs.getBoolean("record_history",false);

                    if (record_history){
                        try {
                            saveRecord(resultContent,rotatedBitmap(bitmap));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (continuous==false){
                        imageView.setImageBitmap(rotatedBitmap(bitmap));
                        imageView.setVisibility(View.VISIBLE);
                    }else{
                        imageView.setVisibility(View.INVISIBLE);
                    }
                } else if (imageView.getVisibility()!=View.VISIBLE) {
                    Log.d("DBR", "No barcode found");
                    textView.setText("");
                }
            }
        });

    }

    private Bitmap rotatedBitmap(Bitmap bitmap){
        Matrix m = new Matrix();
        m.postRotate(camera.getCameraInfo().getSensorRotationDegrees());
        Bitmap bitmapRotated = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,false);
        return bitmapRotated;
    }

    private void overlay(TextResult[] results,Bitmap bitmap) {
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.RED);
        for (int i=0;i<results.length;i++){
            TextResult result = results[i];
            Point[] points=result.localizationResult.resultPoints;
            c.drawLine(points[0].x,points[0].y,points[1].x,points[1].y,p);
            c.drawLine(points[1].x,points[1].y,points[2].x,points[2].y,p);
            c.drawLine(points[2].x,points[2].y,points[3].x,points[3].y,p);
            c.drawLine(points[3].x,points[3].y,points[0].x,points[0].y,p);
        }
    }

    private void saveRecord(String result,Bitmap image) throws IOException {
        if (image!=null){
            Long timestamp=System.currentTimeMillis();
            File path = this.getExternalFilesDir(null);
            File imgfile = new File(path, timestamp+".jpg");
            File txtfile = new File(path, timestamp+".txt");
            Log.d("DBR", imgfile.getAbsolutePath());
            Boolean save_image = prefs.getBoolean("save_image",false);
            if (save_image){
                FileOutputStream outStream = new FileOutputStream(imgfile);
                image.compress(Bitmap.CompressFormat.JPEG,50,outStream);
                outStream.close();
            }
            FileOutputStream outStream2 = new FileOutputStream(txtfile);
            outStream2.write(result.getBytes(Charset.defaultCharset()));
            outStream2.close();
        }
    }


    //use YuvToRgbConverter instead
    private Bitmap imageProxyToBitmap(byte[] bytes,int[] strides,int width, int height) throws IOException {
        @SuppressLint("UnsafeExperimentalUsageError")

        YuvImage yuvImage = new YuvImage(bytes, ImageFormat.NV21, width, height, strides);
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


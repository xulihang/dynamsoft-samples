package com.dynamsoft.camerax;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.MotionEvent;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView textView;
    private ImageView imageView;
    private ImageView viewFinder;
    private BarcodeReader dbr;
    private ExecutorService exec;
    private Camera camera;
    private SharedPreferences prefs;
    private Long TouchDownTime;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        exec = Executors.newSingleThreadExecutor();
        mp = MediaPlayer.create(getApplicationContext(), R.raw.beepsound);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        previewView = findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        textView = findViewById(R.id.resultView);
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        viewFinder = findViewById(R.id.viewFinder);
        try {
            dbr = new BarcodeReader("t0077xQAAAEoQXMjVnF7S9ar4W6em9rhE6UN4uhNa+YU3O8VoTOiYEG2LOvx/G5HZYmRRsWXXHDMr+z0wUHfFh1aBqBJJZ3z1KUd/ACB1Kag=");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }

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

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(hasWindowFocus){
            if (prefs.getBoolean("enable_viewfinder",false)==false){
                viewFinder.setVisibility(View.INVISIBLE);
            }else{
                drawViewFinder();
            }
        }
    }

    public void torchToggled(View view){
        ToggleButton btn= (ToggleButton) view;
        camera.getCameraControl().enableTorch(btn.isChecked());
    }

    public void result_Clicked(View view){
        TextView tv = (TextView) view;
        ClipboardManager myClipboard;
        myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip;
        myClip = ClipData.newPlainText("text", tv.getText());
        myClipboard.setPrimaryClip(myClip);
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void drawViewFinder(){
        int width=viewFinder.getWidth();
        int height=viewFinder.getHeight();
        Bitmap bm= Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setStrokeWidth(2);
        p.setColor(Color.GRAY);
        p.setStyle(Paint.Style.STROKE);
        c.drawRoundRect(0,0,width,height,10,10,p);
        viewFinder.setImageBitmap(bm);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        Size resolution = new Size(720, 1280);
        Display d = getDisplay();
        if (d.getRotation()!=Surface.ROTATION_0){
            resolution = new Size(1280, 720);
        }

        Preview preview = new Preview.Builder().setTargetResolution(resolution).build();

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(resolution)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(exec, new ImageAnalysis.Analyzer() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void analyze(@NonNull ImageProxy image) {
                if (imageView.getVisibility()==View.VISIBLE && prefs.getBoolean("continuous",false)==false){ //non-continuous, scanned
                    image.close();
                    return;
                }

                int rotationDegrees =image.getImageInfo().getRotationDegrees();
                Image im = image.getImage();
                Bitmap bitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(), Bitmap.Config.ARGB_8888);
                TextResult[] results = null;
                Boolean viewFinderEnabled = (viewFinder.getVisibility()==View.VISIBLE);
                if (viewFinderEnabled){
                    YuvToRgbConverter converter = new YuvToRgbConverter(CameraActivity.this);
                    converter.yuvToRgb(image.getImage(),bitmap);
                    bitmap=rotatedBitmap(bitmap,rotationDegrees);
                    rotationDegrees=0;
                    bitmap=croppedBitmap(bitmap);
                    results = decodeBitmap(bitmap);
                } else{
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    int nRowStride = image.getPlanes()[0].getRowStride();
                    int nPixelStride = image.getPlanes()[0].getPixelStride();
                    int length= buffer.remaining();
                    byte[] bytes= new byte[length];
                    buffer.get(bytes);
                    ImageData imageData= new ImageData(bytes,image.getWidth(), image.getHeight(),nRowStride *nPixelStride);
                    results = decodeBuffer(imageData);
                }

                if (imageView.getVisibility()==View.INVISIBLE){
                    if (results.length>0){
                        if (viewFinderEnabled==false){
                            YuvToRgbConverter converter = new YuvToRgbConverter(CameraActivity.this);
                            converter.yuvToRgb(image.getImage(),bitmap);
                        }
                        updateResult(bitmap,results,rotationDegrees );
                    } else{
                        updateResult(results);
                    }
                }
                image.close();
            }
        });


        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .build();
        camera=cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, useCaseGroup);
    }

    private TextResult[] decodeBuffer(ImageData imageData){
        TextResult[] results = new TextResult[0];
        try {
            results = dbr.decodeBuffer(imageData.mBytes,imageData.mWidth,imageData.mHeight, imageData.mStride, EnumImagePixelFormat.IPF_NV21,"");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        return results;
    }

    private TextResult[] decodeBitmap(Bitmap bm){
        TextResult[] results = new TextResult[0];
        try {
            results = dbr.decodeBufferedImage(bm,"");
        } catch (BarcodeReaderException | IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    private Bitmap croppedBitmap(Bitmap bm){
        int left= (int) (((double) viewFinder.getLeft()/previewView.getWidth()) * bm.getWidth());
        int top= (int) (((double) viewFinder.getTop()/previewView.getHeight()) * bm.getHeight());
        int width= (int) (((double) viewFinder.getWidth()/previewView.getWidth()) * bm.getWidth());
        int height= (int) (((double) viewFinder.getHeight()/previewView.getHeight()) * bm.getHeight());
        Bitmap cropped = Bitmap.createBitmap(bm,left,top,width,height);
        return cropped;
    }

    private Bitmap rotatedBitmap(Bitmap bitmap,int rotationDegrees){
        Matrix m = new Matrix();
        m.postRotate(rotationDegrees);
        Bitmap bitmapRotated = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,false);
        return bitmapRotated;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_UP){
            Long heldTime = System.currentTimeMillis()-TouchDownTime;

            if (heldTime>600){ //long press
                focus(previewView.getWidth(),previewView.getHeight(),event.getX(),event.getY(),false);
            } else{
                Toast.makeText(this, "Refocus after 5 seconds.", Toast.LENGTH_SHORT).show();
                focus(previewView.getWidth(),previewView.getHeight(),event.getX(),event.getY(),true);
            }

        } else if (event.getAction() == MotionEvent.ACTION_DOWN){
            TouchDownTime=System.currentTimeMillis();
        }
        return super.onTouchEvent(event);
    }

    private void focus(float width, float height, float x, float y, boolean autoCancel){
        CameraControl cameraControl=camera.getCameraControl();
        MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(width, height);
        MeteringPoint point = factory.createPoint(x, y);
        FocusMeteringAction.Builder builder = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF);
        if (autoCancel){
            // auto calling cancelFocusAndMetering in 5 seconds
            builder.setAutoCancelDuration(5, TimeUnit.SECONDS);
        }else{
            builder.disableAutoCancel();
        }
        FocusMeteringAction action =builder.build();
        ListenableFuture future = cameraControl.startFocusAndMetering(action);
    }

    private void updateResult(TextResult[] results){
        updateResult(null,results,0);
    }

    private void updateResult(Bitmap bitmap,TextResult[] results,int rotationDegrees ){
        this.runOnUiThread(new Runnable() {
            @SuppressLint("UnsafeExperimentalUsageError")
            @Override
            public void run() {
                if (results.length > 0) {
                    Boolean continuous = prefs.getBoolean("continuous",false);
                    Boolean record_history = prefs.getBoolean("record_history",false);
                    Boolean beep = prefs.getBoolean("beep",false);
                    overlay(results,bitmap);
                    String resultContent = "Found " + results.length + " barcode(s):\n";
                    for (int i = 0; i < results.length; i++) {
                        resultContent += results[i].barcodeText + "\n";
                    }
                    Log.d("DBR", resultContent);
                    textView.setText(resultContent);
                    if (beep){
                        playBeepSound();
                    }
                    if (record_history){
                        try {
                            saveRecord(resultContent,rotatedBitmap(bitmap,rotationDegrees ));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (continuous==false){
                        imageView.setImageBitmap(rotatedBitmap(bitmap,rotationDegrees ));
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

    private void playBeepSound(){
        mp.start();
    }

    private void overlay(TextResult[] results,Bitmap bitmap) {
        Canvas c = new Canvas(bitmap);
        Paint p = new Paint();
        p.setStrokeWidth(5);
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


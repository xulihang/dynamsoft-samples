package com.example.dbr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumImagePixelFormat;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.CameraListener;
import com.dynamsoft.dce.CameraState;
import com.dynamsoft.dce.CameraView;
import com.dynamsoft.dce.Frame;
import com.dynamsoft.dce.HardwareUtil;
import com.dynamsoft.dce.Resolution;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DCEActivity extends AppCompatActivity {
    private CameraEnhancer mCamera;
    private CameraView cameraView;
    private BarcodeReader dbr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_c_e);
        try {
            dbr = new BarcodeReader("t0077xQAAAEoQXMjVnF7S9ar4W6em9rhE6UN4uhNa+YU3O8VoTOiYEG2LOvx/G5HZYmRRsWXXHDMr+z0wUHfFh1aBqBJJZ3z1KUd/ACB1Kag=");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        cameraView = findViewById(R.id.cameraView);
        //Initialize your camera
        mCamera = new CameraEnhancer(this);
        mCamera.addCameraView(cameraView);
        //Set camera on
        try {
            mCamera.setCameraDesiredState(CameraState.CAMERA_STATE_ON);
        } catch (CameraEnhancerException e) {
            e.printStackTrace();
        }
        mCamera.addCameraListener(new CameraListener() {
            @Override
            public void onPreviewOriginalFrame(Frame frame) {
                Log.d("DBR", "original");
            }
            @Override
            public void onPreviewFilterFrame(Frame frame) {
                Log.d("DBR", "filter");
            }

            @Override
            public void onPreviewFastFrame(Frame frame) {
                Log.d("DBR", "fastframe");
                TextResult[] results = new TextResult[0];
                try {
                    results = dbr.decodeBuffer(frame.getData(),frame.getWidth(),frame.getHeight(),frame.getStrides()[0],frame.getFormat(),"");
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }

                if (results.length > 0) {
                    String resultContent = "Found " + results.length + " barcode(s):\n";
                    for (int i = 0; i < results.length; i++) {
                        resultContent += results[i].barcodeText + "\n";
                    }
                    Log.d("DBR", resultContent);
                    Intent data = new Intent();
                    data.putExtra("TextResult",resultContent);
                    setResult(20, data);
                    finish();
                } else {
                    Log.d("DBR", "No barcode found");
                }
            }
        });
        boolean ifNeedFilter = true;
        boolean ifNeedAutoFocus = true;
        int deviceLevel = mCamera.getDeviceLevel();
        if (deviceLevel == HardwareUtil.DEVICE_LEVEL_HIGH)
        {
            ifNeedFilter = false;
            ifNeedAutoFocus = false;
        }
        if(deviceLevel == HardwareUtil.DEVICE_LEVEL_MID)
        {
            ifNeedAutoFocus = false;
        }
        mCamera.setForceAutoFocus(ifNeedAutoFocus);
        mCamera.setResolution(Resolution.RESOLUTION_720P);
        mCamera.setUseFrameFilter(false);
        mCamera.setFastMode(false);
        //Start Scan
        try {
            mCamera.startScanning();
        } catch (CameraEnhancerException e) {
            e.printStackTrace();
        }
    }
}
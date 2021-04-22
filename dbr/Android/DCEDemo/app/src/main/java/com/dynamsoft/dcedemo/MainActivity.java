package com.dynamsoft.dcedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.CameraListener;
import com.dynamsoft.dce.CameraState;
import com.dynamsoft.dce.CameraView;
import com.dynamsoft.dce.Frame;
import com.dynamsoft.dce.HardwareUtil;
import com.dynamsoft.dce.Resolution;

public class MainActivity extends AppCompatActivity {
    private CameraEnhancer mCamera;
    private CameraView cameraView;
    private TextView resultView;
    private BarcodeReader dbr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            dbr = new BarcodeReader("t0077xQAAAEoQXMjVnF7S9ar4W6em9rhE6UN4uhNa+YU3O8VoTOiYEG2LOvx/G5HZYmRRsWXXHDMr+z0wUHfFh1aBqBJJZ3z1KUd/ACB1Kag=");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        resultView=findViewById(R.id.resultView);
        startDCE();
    }

    private void startDCE(){
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
                TextResult[] results = new TextResult[0];
                try {
                    results = dbr.decodeBuffer(frame.getData(),frame.getWidth(),frame.getHeight(),frame.getStrides()[0],frame.getFormat(),"");
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Found ").append(results.length).append(" barcode(s):\n");
                if (results.length > 0) {
                    for (int i = 0; i < results.length; i++) {
                        sb.append(results[i].barcodeText);
                        sb.append("\n");
                    }
                    Log.d("DBR", sb.toString());
                } else {
                    Log.d("DBR", "No barcode found");
                }
                resultView.setText(sb.toString());
            }
            @Override
            public void onPreviewFilterFrame(Frame frame) {
                Log.d("DBR", "filter");
            }

            @Override
            public void onPreviewFastFrame(Frame frame) {
                Log.d("DBR", "fastframe");
            }
        });

        //mCamera.setAutoFocus(true);
        //mCamera.setResolution(Resolution.RESOLUTION_720P);
        //mCamera.enableFrameFilter(false);
        //mCamera.enableFastMode(false);

        //Start Scan
        try {
            mCamera.startScanning();
        } catch (CameraEnhancerException e) {
            e.printStackTrace();
        }
    }
}
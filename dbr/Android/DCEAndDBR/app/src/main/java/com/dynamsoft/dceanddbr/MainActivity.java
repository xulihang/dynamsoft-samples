package com.dynamsoft.dceanddbr;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DCESettingParameters;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dbr.TextResultCallback;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.CameraLTSLicenseVerificationListener;
import com.dynamsoft.dce.CameraListener;
import com.dynamsoft.dce.CameraState;
import com.dynamsoft.dce.CameraView;
import com.dynamsoft.dce.Frame;

import java.util.ArrayList;

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
        com.dynamsoft.dce.DMLTSConnectionParameters info = new com.dynamsoft.dce.DMLTSConnectionParameters();
        // The organization id 200001 here will grant you a public trial license good for 7 days.
        // After that, you can send an email to trial@dynamsoft.com
        // (make sure to include the keyword privateTrial in the email title)
        // to obtain a 30-day free private trial license which will also come in the form of an organization id.
        info.organizationID = "200001";
        mCamera.initLicenseFromLTS(info,new CameraLTSLicenseVerificationListener() {
            @Override
            public void LTSLicenseVerificationCallback(boolean isSuccess, Exception error) {
                if(!isSuccess){
                    error.printStackTrace();
                }

            }
        });


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
            }
        });

        //mCamera.setAutoFocus(true);
        //mCamera.setResolution(Resolution.RESOLUTION_720P);
        mCamera.enableFrameFilter(true);
        mCamera.enableSensorControl(true);
        //mCamera.enableFastMode(false);
        TextResultCallback mTextResultCallback = new TextResultCallback() {
            @Override
            public void textResultCallback(int i, TextResult[] textResults, Object o) {
                if (textResults != null && textResults.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Found ").append(textResults.length).append(" barcode(s):\n");
                    if (textResults.length > 0) {
                        for (int j = 0; j < textResults.length; j++) {
                            TextResult tr = textResults[j];
                            sb.append(tr.barcodeText);
                            sb.append("\n");
                        }
                        Log.d("DBR", sb.toString());
                    } else {
                        Log.d("DBR", "No barcode found");
                    }
                    resultView.setText(sb.toString());
                }
            }
        };
        cameraView.addOverlay();
        DCESettingParameters dceSettingParameters = new DCESettingParameters();
        dceSettingParameters._cameraInstance = mCamera;
        dceSettingParameters._textResultCallback=mTextResultCallback;
        dbr.SetCameraEnhancerParam(dceSettingParameters);
        //Start Scan
        dbr.StartCameraEnhancer();

    }
}
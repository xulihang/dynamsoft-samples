package com.dynamsoft.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.Point;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.CameraLTSLicenseVerificationListener;
import com.dynamsoft.dce.CameraListener;
import com.dynamsoft.dce.CameraState;
import com.dynamsoft.dce.CameraView;
import com.dynamsoft.dce.Frame;
import com.dynamsoft.dce.Resolution;
import com.dynamsoft.dlr.DLRImageData;
import com.dynamsoft.dlr.DLRLTSLicenseVerificationListener;
import com.dynamsoft.dlr.DLRResult;
import com.dynamsoft.dlr.DMLTSConnectionParameters;
import com.dynamsoft.dlr.EnumDLRImagePixelFormat;
import com.dynamsoft.dlr.LabelRecognition;
import com.dynamsoft.dlr.LabelRecognitionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LiveScanActivity extends AppCompatActivity {
    private CameraEnhancer mCamera;
    private CameraView cameraView;
    private TextView resultView;
    private BarcodeReader dbr;
    private LabelRecognition dlr;
    private Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_scan);
        resultView=findViewById(R.id.resultView_dce);
        ctx=this;
        try {
            dbr = new BarcodeReader("t0068MgAAAJWPwDybm7nk0f9xYH25MMaVrZYcmhsiVoZrVo2hfcwRS74T6QA79OfzyvhC+9fgFI2noI8zBc66WHFCusVUgqk=");
        } catch (BarcodeReaderException barcodeReaderException) {
            barcodeReaderException.printStackTrace();
        }
        initDLR();
        initDCE();
    }

    private void initDLR(){
        try {
            dlr = new LabelRecognition();
            DMLTSConnectionParameters parameters;
            parameters = new DMLTSConnectionParameters();
            parameters.organizationID = "200001";
            dlr.initLicenseFromLTS(parameters, new DLRLTSLicenseVerificationListener() {
                @Override
                public void LTSLicenseVerificationCallback(boolean isSuccess, final Exception error) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void initDCE(){
        cameraView = findViewById(R.id.cameraView);
        //Initialize your camera
        mCamera = new CameraEnhancer(this);
        com.dynamsoft.dce.DMLTSConnectionParameters info = new com.dynamsoft.dce.DMLTSConnectionParameters();
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
        cameraView.addOverlay();

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
                Log.d("DBR", "orientation: "+frame.getOrientation());
            }
            @Override
            public void onPreviewFilterFrame(Frame frame) {
                Log.d("DBR", "filter");
            }

            @Override
            public void onPreviewFastFrame(Frame frame) {
                Log.d("DBR", "fastframe");
                Log.d("DBR", "orientation: "+frame.getOrientation());
                Log.d("DBR", "width: "+frame.width);
                Log.d("DBR", "height: "+frame.height);
                decode(frame);
            }
        });

        Resolution res = Resolution.DEFALUT;
        mCamera.setResolution(res);
        mCamera.startScanning();
    }

    private void decode(Frame frame){
        String resultString="";
        TextResult[] results = new TextResult[0];
        DLRResult[] dlrResults = new DLRResult[0];
        try {
            //results = dbr.decodeBufferedImage(bitmap, "");
            results = dbr.decodeBuffer(frame.getData(),frame.getWidth(),frame.getHeight(),frame.getStrides()[0],frame.getFormat(),"");
            String templateName = LoadDLRTemplate();
            Log.d("DBR",templateName);
            dlr.updateReferenceRegionFromBarcodeResults(results,templateName);
            DLRImageData imageData= new DLRImageData();
            imageData.bytes = frame.getData();
            imageData.width = frame.getWidth();
            imageData.height = frame.getHeight();
            imageData.format = EnumDLRImagePixelFormat.DLR_IPF_NV21;
            imageData.stride = frame.getStrides()[0];
            dlrResults = dlr.recognizeByBuffer(imageData,templateName);
        } catch (BarcodeReaderException | LabelRecognitionException e) {
            e.printStackTrace();
        }

        ArrayList<android.graphics.Point> points = new ArrayList<>();
        if (results != null && results.length > 0) {
            UpdateLocationIfFastFrame(results,frame);
            points.addAll(Utils.GetResultPointsArrayListFromTextResults(results));
        }
        if (dlrResults != null && dlrResults.length > 0) {
            UpdateDLRLocationIfFastFrame(dlrResults,frame);
            points.addAll(Utils.GetResultPointsArrayListFromDLRResults(dlrResults));
        }
        resultString=Utils.getBarcodeResult(results)+Utils.getLabelResult(dlrResults);
        mCamera.setResultPoints(points); //show overlay
        Log.d("DBR", "DBR: "+results.length);
        Log.d("DBR", "DLR: "+dlrResults.length);
        if (results.length==0 && dlrResults.length==0){
            UpdateResult("No data found");
        } else{
            UpdateResult(resultString);
        }
    }

    private void UpdateResult(String result){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.
                resultView.setText(result);
            }
        });
    }
    private void UpdateLocationIfFastFrame(TextResult[] results, Frame frame){
        if (frame.isFastFrame()) {
            Rect rect = frame.getCropRect();
            if (rect != null){
                for (int i = 0; i < results.length; i++) {
                    for (int j = 0; j < 4; j++) {
                        ((results[i]).localizationResult.resultPoints[j]).x += rect.left;
                        ((results[i]).localizationResult.resultPoints[j]).y += rect.top;
                    }
                }
            }
        }
    }

    private void UpdateDLRLocationIfFastFrame(DLRResult[] results, Frame frame){
        if (frame.isFastFrame()) {
            Rect rect = frame.getCropRect();
            if (rect != null){
                for (int i = 0; i < results.length; i++) {
                    for (int j = 0; j < 4; j++) {
                        ((results[i]).location.points[j]).x += rect.left;
                        ((results[i]).location.points[j]).y += rect.top;
                    }
                }
            }
        }
    }

    private String LoadDLRTemplate()
    {
        String templateName = "P1";
        try {
            dlr.clearAppendedSettings();
            dlr.appendSettingsFromString(Utils.readRawTextFile(ctx,R.raw.template));
        } catch (LabelRecognitionException e) {
            e.printStackTrace();
        }
        return templateName;
    }

}
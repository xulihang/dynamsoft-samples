package com.dynamsoft.dcesimplesample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.DBRLTSLicenseVerificationListener;
import com.dynamsoft.dbr.DCESettingParameters;
import com.dynamsoft.dbr.EnumIntermediateResultSavingMode;
import com.dynamsoft.dbr.EnumIntermediateResultType;
import com.dynamsoft.dbr.EnumResultCoordinateType;
import com.dynamsoft.dbr.IntermediateResult;
import com.dynamsoft.dbr.LocalizationResult;
import com.dynamsoft.dbr.Point;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dbr.TextResultCallback;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraEnhancerException;
import com.dynamsoft.dce.CameraLTSLicenseVerificationListener;
import com.dynamsoft.dce.CameraListener;
import com.dynamsoft.dce.CameraView;
import com.dynamsoft.dce.Frame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private CameraView cameraView;
    private BarcodeReader reader;
    private CameraEnhancer mCameraEnhancer;
    private TextView resultView;
    private TextView touchView;
    private Context ctx;
    private int lastFrameId = -1;
    private Timer timer = new Timer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);
        resultView = findViewById(R.id.resultView);
        ctx=this;
        initDBR();
        updateRuntimeSettings();
        initDCE();
        setupTouchEvent();
        startScanning(2);
    }

    private void startScanning(int option){
        switch (option){
            case 0:
                Log.d("DBR","Option 1");
                setDCECallback(); //Option 1: use callbacks
                mCameraEnhancer.startScanning();
                break;
            case 1:
                Log.d("DBR","Option 2");
                passDCEtoDBR(); //Option 2: use DBR to control DCE
                reader.StartCameraEnhancer();
                break;
            case 2:
                Log.d("DBR","Option 3");
                mCameraEnhancer.startScanning();
                timer.scheduleAtFixedRate(task, 1000, 100); //Option 3: use frame queue
                break;
            default:
                Log.d("DBR","Option Default");
                setDCECallback();
                mCameraEnhancer.startScanning();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchEvent(){
        touchView = findViewById(R.id.touchView);
        touchView.setLongClickable(true);
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = -1, y = -1;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    x = (int) event.getX();
                    y = (int) event.getY();
                }
                if (x != -1 && y != -1) {
                    mCameraEnhancer.setManualFocusPosition(x, y);
                    Log.d("DBR","Manual Focus");
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        mCameraEnhancer.startScanning();
        super.onResume();
    }

    @Override
    public void onPause() {
        mCameraEnhancer.stopScanning();
        super.onPause();
    }

    private void initDBR(){
        try {
            reader = new BarcodeReader();
            com.dynamsoft.dbr.DMLTSConnectionParameters parameters = new com.dynamsoft.dbr.DMLTSConnectionParameters();
            parameters.organizationID = "200001";
            reader.initLicenseFromLTS(parameters, new DBRLTSLicenseVerificationListener() {
                @Override
                public void LTSLicenseVerificationCallback(boolean b, Exception e) {
                    if (!b) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
    }

    private void initDCE(){
        mCameraEnhancer = new CameraEnhancer(MainActivity.this);
        mCameraEnhancer.addCameraView(cameraView);
        com.dynamsoft.dce.DMLTSConnectionParameters info = new com.dynamsoft.dce.DMLTSConnectionParameters();
        info.organizationID = "200001";
        mCameraEnhancer.initLicenseFromLTS(info, new CameraLTSLicenseVerificationListener() {
            @Override
            public void LTSLicenseVerificationCallback(boolean isSuccess, Exception e) {
                if (!isSuccess) {
                    e.printStackTrace();
                }
            }
        });
        cameraView.addOverlay();
        mCameraEnhancer.enableFastMode(true);
        mCameraEnhancer.enableAutoZoom(true);
    }

    private void passDCEtoDBR(){
        //Get the text result from Dynamsoft Barcode Reader
        TextResultCallback mTextResultCallback = new TextResultCallback() {
            @Override
            public void textResultCallback(int i, TextResult[] textResults, Object o) {
                showResult(textResults,null);
            }
        };
        //Set DCE setting parameters in Dynamsoft Barcode Reader
        DCESettingParameters dceSettingParameters = new DCESettingParameters();
        dceSettingParameters._cameraInstance = mCameraEnhancer;
        dceSettingParameters._textResultCallback = mTextResultCallback;
        //Instantiate DCE, send result and immediate result call back to Dynamsoft Barcode Reader
        reader.SetCameraEnhancerParam(dceSettingParameters);
    }

    private void setDCECallback(){
        mCameraEnhancer.addCameraListener(new CameraListener() {
            @Override
            public void onPreviewOriginalFrame(Frame frame) {


            }
            @Override
            public void onPreviewFilterFrame(Frame frame) {

            }

            @Override
            public void onPreviewFastFrame(Frame frame) {
                decode(frame);
            }
        });
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Frame frame = mCameraEnhancer.AcquireListFrame(true);
            if (frame!=null){
                if (frame.getFrameId()!=lastFrameId){
                    lastFrameId=frame.getFrameId();
                    decode(frame);
                }
            }
        }
    };

    private void decode(Frame frame){
        try {
            TextResult[] results = reader.decodeBuffer(frame.getData(),frame.getWidth(),frame.getHeight(),frame.getStrides()[0],frame.getFormat(),"");
            UpdateLocationIfFastFrame(results,frame);
            showResult(results,frame);
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
    }

    private void saveFrame(Frame frame){
        Long timestamp = System.currentTimeMillis();
        File path = ctx.getExternalFilesDir(null);
        File imgfile = new File(path, timestamp + ".jpg");
        Bitmap bitmap = FrameToBitmap(frame);
        try {
            FileOutputStream outStream  = new FileOutputStream(imgfile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap FrameToBitmap(Frame frame){
        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        Rect rect = new Rect();
        rect.left=0;
        rect.top=0;
        rect.bottom=frame.height;
        rect.right=frame.width;
        YuvImage image = new YuvImage(frame.data, ImageFormat.NV21, frame.width, frame.height, frame.strides);
        image.compressToJpeg(rect,100,outputSteam);
        byte[] b = outputSteam.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bitmap;
    }


    private void showResult(TextResult[] results,Frame frame) {
        if (results != null && results.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Result:").append("\n");
            ArrayList<android.graphics.Point> arrayList = new ArrayList<>();
            for (int i = 0; i < results.length; i++){
                sb.append(results[i].barcodeText).append("\n");
                arrayList.addAll(PointsAsArrayList(results[i].localizationResult.resultPoints));
            }
            resultView.setText(sb.toString());
            mCameraEnhancer.setResultPoints(arrayList);

        }else{
            mCameraEnhancer.setResultPoints(new ArrayList<>());
            resultView.setText("No barcodes found.");
            if (frame!=null){
                Point[] resultPoints = new Point[0];
                try {
                    resultPoints = getResultsPointsWithHighestConfidence(reader.getIntermediateResults());
                    Log.d("DBR", "result points: "+resultPoints);
                    if (resultPoints!=null) {
                        Log.d("DBR", "autozoom");
                        //mCameraEnhancer.setResultPoints(PointsAsArrayList(resultPoints));
                        mCameraEnhancer.setZoomRegion(GetRect(resultPoints,frame),frame.getOrientation()); //autozoom
                    }
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private ArrayList<android.graphics.Point> PointsAsArrayList(Point[] points){
        ArrayList<android.graphics.Point> arrayList = new ArrayList<>();
        for (com.dynamsoft.dbr.Point point:points){
            android.graphics.Point newPoint = new android.graphics.Point();
            newPoint.x=point.x;
            newPoint.y=point.y;
            arrayList.add(newPoint);
        }
        return arrayList;
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

    private void updateRuntimeSettings(){
        try {
            PublicRuntimeSettings rs = reader.getRuntimeSettings();
            rs.intermediateResultTypes= EnumIntermediateResultType.IRT_TYPED_BARCODE_ZONE;
            rs.intermediateResultSavingMode= EnumIntermediateResultSavingMode.IRSM_MEMORY;
            rs.resultCoordinateType= EnumResultCoordinateType.RCT_PIXEL;
            reader.updateRuntimeSettings(rs);
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
    }

    private Point[] getResultsPointsWithHighestConfidence(IntermediateResult[] intermediateResults){
        for (IntermediateResult ir:intermediateResults){
            if (ir.resultType== EnumIntermediateResultType.IRT_TYPED_BARCODE_ZONE){
                int maxConfidence=0;
                for (Object result:ir.results)
                {
                    LocalizationResult lr = (LocalizationResult) result;
                    maxConfidence=Math.max(lr.confidence,maxConfidence);
                    Log.d("DBR", "confidence: "+lr.confidence);
                }
                Log.d("DBR", "max confidence: "+maxConfidence);
                for (Object result:ir.results)
                {
                    LocalizationResult lr = (LocalizationResult) result;
                    if (lr.confidence==maxConfidence && maxConfidence>80){
                        Log.d("DBR", "x: "+lr.resultPoints[0].x);
                        Log.d("DBR", "y: "+lr.resultPoints[0].y);
                        return lr.resultPoints;
                    }
                }
            }
        }
        return null;
    }

    private Rect GetRect(Point[] points, Frame frame) {
        int leftX = (points[0]).x, rightX = leftX;
        int leftY = (points[0]).y, rightY = leftY;
        for (Point pt : points) {
            if (pt.x < leftX)
                leftX = pt.x;
            if (pt.y < leftY)
                leftY = pt.y;
            if (pt.x > rightX)
                rightX = pt.x;
            if (pt.y > rightY)
                rightY = pt.y;
        }
        if (frame.isFastFrame()) {
            int original_w = frame.getOriW();
            int original_h = frame.getOriH();
            if (frame.getFastFrameId() % 4 == 1) {
                if (frame.getOrientation() == 1) {
                    leftX += original_w / 4;
                    rightX += original_w / 4;
                } else if (frame.getOrientation() == 2) {
                    leftY += original_h / 4;
                    rightY += original_h / 4;
                }
            } else if (frame.getFastFrameId() % 4 != 0) {
                leftX += original_w / 4;
                rightX += original_w / 4;
                leftY += original_h / 4;
                rightY += original_h / 4;
            }
        }
        Rect frameRegion = new Rect(leftX, leftY, rightX, rightY);
        return frameRegion;
    }
}

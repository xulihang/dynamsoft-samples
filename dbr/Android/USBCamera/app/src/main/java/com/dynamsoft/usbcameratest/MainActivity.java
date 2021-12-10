package com.dynamsoft.usbcameratest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.TextResult;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity  implements CameraDialog.CameraDialogParent {

    /**
     * set true if you want to record movie using MediaSurfaceEncoder
     * (writing frame data into Surface camera from MediaCodec
     *  by almost same way as USBCameratest2)
     * set false if you want to record movie using MediaVideoEncoder
     */
    private static final boolean USE_SURFACE_ENCODER = false;
    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640; // 640
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480; //480
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 0; // YUV
    private UVCCameraHandler mCameraHandler;

    private CameraViewInterface mUVCCameraView;
    private ImageButton mCameraButton;
    private USBMonitor mUSBMonitor;
    private BarcodeReader barcodeReader;
    private TextView resultTextView;
    private Timer timer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.resultTextView);
        mCameraButton = findViewById(R.id.imageButton);
        mCameraButton.setOnClickListener(mOnClickListener);
        try {
            barcodeReader = new BarcodeReader("t0068NQAAAABqtDczOvXnHchYhEDwihE9AlhgUoufsIS8/fvgiSB9oU4x8Q4zTN47N7ksSXWCH8bfFREbMklPNnt39BdcfSo=");
        } catch (BarcodeReaderException e) {
            e.printStackTrace();
        }
        final View view = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface)view;
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (double)PREVIEW_HEIGHT);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView,
                USE_SURFACE_ENCODER ? 0 : 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if ((mCameraHandler != null) && !mCameraHandler.isOpened()) {
                CameraDialog.showDialog(MainActivity.this);
            } else {
                mCameraHandler.close();
            }
        }
    };

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            if (mCameraHandler != null) {
                mCameraHandler.open(ctrlBlock);
                startPreview();
                startDecoding();
            }

        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            stopDecoding();
        }
        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    private void startPreview() {
        if (mCameraHandler != null) {
            final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
            mCameraHandler.startPreview(new Surface(st));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
    }

    @Override
    protected void onStop() {
        mUSBMonitor.unregister();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        super.onDestroy();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean b) {

    }

    private void startDecoding(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Bitmap bmp = mUVCCameraView.captureStillImage();
                decode(bmp);
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(task, 1000, 100);
    }
    private void stopDecoding(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

    private void decode(final Bitmap bitmap){
        try {
            final TextResult[] results = barcodeReader.decodeBufferedImage(bitmap,"");
            Log.d("DBR",String.valueOf(results.length));
            runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Found ");
                        sb.append(results.length);
                        sb.append(" barcodes:");
                        sb.append("\n");
                        for (TextResult tr : results){
                            sb.append(tr.barcodeText);
                            sb.append("\n");
                        }
                        resultTextView.setText(sb.toString());
                    }
                }
            );
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
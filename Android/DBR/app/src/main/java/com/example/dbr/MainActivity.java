package com.example.dbr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.TextResult;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    private void loadImage(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICKFILE_REQUEST_CODE);
        } else{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,PICKFILE_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE) {
            Log.d("DBR",data.getData().getPath());
            String folderPath = getRealPathFromURI_API19(this,data.getData());
            Toast.makeText(this, folderPath, Toast.LENGTH_SHORT).show();
            Log.d("DBR",folderPath);
            BarcodeReader dbr = null;
            try {
                dbr = new BarcodeReader("t0077xQAAAEoQXMjVnF7S9ar4W6em9rhE6UN4uhNa+YU3O8VoTOiYEG2LOvx/G5HZYmRRsWXXHDMr+z0wUHfFh1aBqBJJZ3z1KUd/ACB1Kag=");
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
            }

            TextResult[] results = new TextResult[0];
            try {
                results = dbr.decodeFile(folderPath, "");
            } catch (BarcodeReaderException e) {
                e.printStackTrace();
            }

            // e.g. TextResult[] results = dbr.decodeFile("/storage/dbr-preview-img/test.jpg", "");
            //Toast.makeText(this, results.length , Toast.LENGTH_LONG).show();
            if (results.length > 0) {
                String resultContent = "Found " + results.length + " barcode(s):\n";
                for (int i = 0; i < results.length; i++) {
                    resultContent += results[i].barcodeText + "\n";
                }
                Log.d("DBR", resultContent);

                TextView resultTextView=findViewById(R.id.resultTextView);
                resultTextView.setMovementMethod(new ScrollingMovementMethod());
                resultTextView.setText(resultContent);
            } else {
                Log.d("DBR", "No barcode found");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PICKFILE_REQUEST_CODE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    loadImage();
                }
                break;

            default:
                break;
        }
    }

    public void readLocalImageButton_Clicked(View view) {
        loadImage();
    }

    public void liveScanButton_Clicked(View view){
        Toast.makeText(this, "not implemented yet" , Toast.LENGTH_LONG).show();
    }
}
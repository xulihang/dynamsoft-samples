package com.dynamsoft.sample;

import android.content.Context;
import android.graphics.Point;

import com.dynamsoft.dbr.TextResult;
import com.dynamsoft.dlr.DLRPoint;
import com.dynamsoft.dlr.DLRResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Utils {

    public static ArrayList<Point> GetResultPointsArrayListFromTextResults(TextResult[] results){
        ArrayList<android.graphics.Point> array = new ArrayList<>();
        for (TextResult result:results){
            for (com.dynamsoft.dbr.Point point:result.localizationResult.resultPoints){
                android.graphics.Point newPoint = new android.graphics.Point();
                newPoint.x=point.x;
                newPoint.y=point.y;
                array.add(newPoint);
            }
        }
        return array;
    }

    public static ArrayList<Point> GetResultPointsArrayListFromDLRResults(DLRResult[] results){
        ArrayList<android.graphics.Point> array = new ArrayList<>();
        for (DLRResult result:results){
            for (DLRPoint point:result.location.points){
                android.graphics.Point newPoint = new android.graphics.Point();
                newPoint.x=point.x;
                newPoint.y=point.y;
                array.add(newPoint);
            }
        }
        return array;
    }

    public static ArrayList<android.graphics.Point> PointsAsArrayList(com.dynamsoft.dbr.Point[] points){
        ArrayList<android.graphics.Point> array = new ArrayList<>();
        for (com.dynamsoft.dbr.Point point:points){
            android.graphics.Point newPoint = new android.graphics.Point();
            newPoint.x=point.x;
            newPoint.y=point.y;
            array.add(newPoint);
        }
        return array;
    }

    public static String getBarcodeResult(TextResult[] results){
        StringBuilder sb = new StringBuilder();
        sb.append("DBR: ");
        if (results.length>0){
            for (int i = 0; i < results.length; i++) {
                sb.append(results[i].barcodeText);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String getLabelResult(DLRResult[] results){
        StringBuilder sb = new StringBuilder();
        sb.append("DLR: ");
        if (results.length>0){
            for (int i = 0; i < results.length; i++) {
                for (int j = 0; j < results[i].lineResults.length; j++) {
                    sb.append(results[i].lineResults[j].text);
                    sb.append("\n");
                }

            }
        }
        return sb.toString();
    }

    public static String readRawTextFile(Context ctx, int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
}

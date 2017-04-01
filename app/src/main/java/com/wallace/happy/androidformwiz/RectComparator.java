package com.wallace.happy.androidformwiz;

import android.util.Log;

import org.opencv.core.RotatedRect;

import java.util.Comparator;

public class RectComparator implements Comparator<RotatedRect> {
    private static final String TAG = "comparator";
    public int compare(RotatedRect r1, RotatedRect r2){
        Log.v(TAG, r1.center.toString() + " - " + r2.center.toString());
        if ((r1.center.y - r2.center.y) >10){
            return 1;
        }
        else if((r1.center.x - r2.center.x) >10){
            return 1;
        }
        return -1;
    }
}

package com.wallace.happy.androidformwiz;

import org.opencv.core.RotatedRect;

import java.util.Comparator;

public class RectComparator implements Comparator<RotatedRect> {
    public int compare(RotatedRect r1, RotatedRect r2){
        if (r1.center.y - r2.center.y >10){
            return 1;
        }
        else if(r1.center.x - r2.center.x >10){
            return 1;
        }
        return -1;
    }
}

package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.core.RotatedRect;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import static android.app.Activity.RESULT_OK;


public class ImageHelper     {
    String test(){
        return "AOK";
    }

    double[] toArray(RotatedRect r){
        double a[] = new double[5];
        a[0] = r.center.x;
        a[1] = r.center.y;
        a[2] = r.size.width;
        a[3] = r.size.height;
        a[4] = r.angle;
        return a;
    }

}


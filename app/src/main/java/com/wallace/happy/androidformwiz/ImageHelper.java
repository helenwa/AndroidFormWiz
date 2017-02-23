package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;
import static org.opencv.imgproc.Imgproc.warpAffine;


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

    Mat drawSquares(List<RotatedRect> squares, Mat image) {
        if (squares.size() == 0) {
            return image;
        }
        Mat cropped = new Mat();
        RotatedRect maxRect = squares.get(0);
        double maxArea = 0;
        for (int i = 0; i < squares.size(); i++) {

            RotatedRect minRect = squares.get(i);
            Point rect_points[] = new Point[4];
            minRect.points(rect_points);
            for (int j = 0; j < 4; j++) {
                Scalar scal = new Scalar(0, 0, 255);
            Imgproc.line( image, rect_points[j], rect_points[(j+1)%4], scal, 1, 8, 0 ); // blue
            }
        }
        return image;
    }

}


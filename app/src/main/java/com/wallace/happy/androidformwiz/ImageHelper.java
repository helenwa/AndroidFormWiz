package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.wallace.happy.androidformwiz.EditFormTemplateActivity.CV_CHAIN_APPROX_SIMPLE_1;
import static com.wallace.happy.androidformwiz.EditFormTemplateActivity.CV_RETR_LIST_1;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
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
                Scalar scal = new Scalar(0, 255, 0);// RGB
            Imgproc.line( image, rect_points[j], rect_points[(j+1)%4], scal, 3, 8, 0 );
            }
        }
        return image;
    }

    Mat rotAndCrop(RotatedRect maxRect, Mat image) {
        // matrices we'll use
        Mat M;
        Mat rotated = new Mat();
        // get angle and size from the bounding box
        double angle = maxRect.angle;
        Size rect_size = maxRect.size;
        // thanks to http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/
        if (maxRect.angle < -45.) {
            Log.v("ImgaeHelper", "swapping h and w");
            angle += 90.0;
            //swap(rect_size.width, rect_size.height);
            rect_size = new Size(rect_size.height, rect_size.width);
        }
        // get the rotation matrix
        M = getRotationMatrix2D(maxRect.center, angle, 1.0);
        // perform the affine transformation
        warpAffine(image, rotated, M, image.size(), INTER_CUBIC);
        // crop the resulting image
        double x = maxRect.center.x - (rect_size.width/2);
        double y = maxRect.center.y - (rect_size.height/2);
        Rect roi = new Rect( (int)x, (int)y, (int)rect_size.width, (int)rect_size.height);
        return  new Mat(rotated, roi);
    }

    RotatedRect findSquaure(Mat image,int w, int h ) {
        // blur will enhance edge detection
        Mat gray0 = new Mat(h, w, CvType.CV_8UC1);
        Imgproc.medianBlur(image, gray0, 9);
        Mat gray = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<RotatedRect> squares = new ArrayList<RotatedRect>();
        Imgproc.Canny(gray0, gray, 20, 80); //
        Point pnt = new Point(-1, -1);
        Imgproc.dilate(gray, gray, new Mat(), pnt, 1);
        Imgproc.findContours(gray, contours, new Mat(), CV_RETR_LIST_1, CV_CHAIN_APPROX_SIMPLE_1);
        RotatedRect max = new RotatedRect();
        // Test contours
        MatOfPoint2f approx = new MatOfPoint2f();

        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(contour2f, approx, Imgproc.arcLength(contour2f, true) * 0.005, true);
            // Log.v(TAG, i + "  " + approx.toArray().length + "  " + abs(Imgproc.contourArea(approx)) + "  " + Imgproc.isContourConvex(contours.get(i)));

            // Note: absolute value of an area is used because
            // area may be positive or negative - in accordance with the
            // contour orientation
            if (
                    approx.toArray().length == 4 &&
                            abs(Imgproc.contourArea(approx)) > 200
                    ) {
                double maxCosine = 0;

                for (int j = 2; j < 5; j++) {
                    double cosine = abs(angle(approx.toArray()[j % 4], approx.toArray()[j - 2], approx.toArray()[j - 1]));
                    //Log.v(TAG, i + ",  " + cosine + ",  " + approx.toArray().toString());
                    maxCosine = max(maxCosine, cosine);
                }

                if (maxCosine < 0.5) {

                    RotatedRect minRect = Imgproc.minAreaRect(contour2f);

                    Point rect_points[] = new Point[4];
                    minRect.points(rect_points);
                    if(minRect.size.area()>max.size.area()){
                        max=minRect;
                    }
                }
            }
        }
        return max;
    }


    double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }


}


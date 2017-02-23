package com.wallace.happy.androidformwiz;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.wallace.happy.androidformwiz.SelectFormTemplateActivity.TEMP_REF;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static java.util.Collections.sort;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;
import static org.opencv.imgproc.Imgproc.warpAffine;

public class EditFormTemplateActivity extends AppCompatActivity {
    public String templateReference;
    private static final String TAG = "EditFormTemp";

    static final int CV_RETR_LIST_1 = 1;
    static final int CV_CHAIN_APPROX_SIMPLE_1 = 2;
    Mat tmp;
    Mat processed;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    //Log.i("OpenCV", "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
           // Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
           // Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_form_template);
        //load image
        Intent intent = getIntent();
        templateReference = intent.getStringExtra(TEMP_REF);


        if (!OpenCVLoader.initDebug()) {
           // Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
           // Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //
        loadImageFromStorage(templateReference);
        processImage();
        putImageOnScreen();

        //insert dropdowns as required
        LayoutInflater vi = getLayoutInflater();
        View v = vi.inflate(R.layout.form_dropdown, null);

        // fill in any details dynamically here
        Spinner spinner = (Spinner) v.findViewById(R.id.form_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.form_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //spinner.setText("your text");

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
        insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    List<RotatedRect> squares;
    private void processImage() {
        //convert
        tmp = new Mat(b.getHeight(), b.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(b, tmp);
        //find rectangles
        squares = findSquaures(tmp);
        //draw rect on image
        processed =rotAndCrop(squares, tmp);
        squares = findSquaures(processed);
        Log.v(TAG, "Hi h");
        for (int i = 0; i < squares.size(); i++) {
            Log.v(TAG, i + "-" + squares.get(i).toString());
            Point pt[] = new Point[4];
            squares.get(i).points(pt);
            Log.v(TAG, i + "-" + pt.toString());
        }
        Log.v(TAG, squares.toString());
        //convert Mat to bitmap
        b = Bitmap.createBitmap(processed.cols(), processed.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(processed, b);
    }

    List<RotatedRect> findSquaures(Mat image) {
// blur will enhance edge detection
        Mat gray0 = new Mat(b.getHeight(), b.getWidth(), CvType.CV_8UC1);
        Imgproc.medianBlur(image, gray0, 9);
        Mat gray = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<RotatedRect> squares = new ArrayList<RotatedRect>();
        Imgproc.Canny(gray0, gray, 20, 80); //
        Point pnt = new Point(-1, -1);
        Imgproc.dilate(gray, gray, new Mat(), pnt, 1);

        Imgproc.findContours(gray, contours, new Mat(), CV_RETR_LIST_1, CV_CHAIN_APPROX_SIMPLE_1);

       // Log.v(TAG, contours.toString());

        // Test contours
        MatOfPoint2f approx = new MatOfPoint2f();

        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(contour2f, approx, Imgproc.arcLength(contour2f, true) * 0.1, true);
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

                if (maxCosine < 0.3) {

                    RotatedRect minRect = Imgproc.minAreaRect(contour2f);
                    Point rect_points[] = new Point[4];
                    minRect.points(rect_points);
                    squares.add(minRect);
                }
            }
        }
        return squares;
    }

    Mat rotAndCrop(List<RotatedRect> squares, Mat image) {
        if (squares.size() == 0) {
            return image;
        }
        Mat cropped;
        RotatedRect maxRect = squares.get(0);
        double maxArea = 0;
        for (int i = 0; i < squares.size(); i++) {
            double area = squares.get(i).size.area();
            if (area > maxArea) {
                maxRect = squares.get(i);
            }
        }
        // matrices we'll use
        Mat M;
        Mat rotated = new Mat();

        // get angle and size from the bounding box
        double angle = maxRect.angle;
        Size rect_size = maxRect.size;
        // thanks to http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/
        if (maxRect.angle < -45.) {
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
        cropped = new Mat(rotated, roi);
        return cropped;
    }

    double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    Bitmap b;
    String p;

    //loads image from file to screen
    private void loadImageFromStorage(String path) {
        try {
            p = path;
            File f = new File(path, "newTemplate.jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void putImageOnScreen() {
        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.setImageBitmap(b);
    }


    private DBHelper db = new DBHelper(this);

    public void saveForm(View view) {
        EditText mEdit = (EditText) findViewById(R.id.editText);
        String nameString = mEdit.getText().toString();
        double w = b.getWidth();
        double h = b.getHeight();
        String id = db.insertForm(nameString, templateReference, squares, w, h);
        saveToInternalStorage(b, id);
        //TODO save box variables in second table
        //toast
        CharSequence text = "Form Saved";
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        goHome();
    }

    //go to homeScreen
    private void goHome() {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Abandon Form?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        goHome();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    //saves image to file
    private String saveToInternalStorage(Bitmap bitmapImage, String name) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath = new File(directory, name + ".jpg");//"newTemplate.jpg");
        String text = directory.getAbsolutePath() + name;
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


}

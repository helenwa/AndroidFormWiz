package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static com.wallace.happy.androidformwiz.SelectWorkingFormActivity.FORM_REF;

public class FormDetailsActivity extends AppCompatActivity {

    private ImageHelper ih = new ImageHelper();
    private DBHelper db = new DBHelper(this);
    String name;
    String idString;
    long id;
    String path;
    private static final String TAG = "DETAILS>>";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
        setContentView(R.layout.activity_form_details);
        Intent intent = getIntent();
        id = getIntent().getLongExtra(FORM_REF, 0) + 1;
        idString = String.valueOf(id);

        if (!OpenCVLoader.initDebug()) {
            // Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            // Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //get details from DB
        Cursor c = db.getData(id);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex("Name"));
        path = c.getString(c.getColumnIndex("ImageSource"));
        int nBoxes = c.getInt(c.getColumnIndex("boxes"));
        boxes = db.getBoxes(id, nBoxes);
        Log.v(TAG, "boxes - " + boxes);
        loadImageFromStorageTOScreen(path, idString);
        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
        nameTextView.setText(name);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    List<RotatedRect> boxes;
    Bitmap b;
    String p;
    Mat tmp;

    //loads image from file to screen
    private void loadImageFromStorageTOScreen(String path, String id) {
        try {
            p = path;
            File f = new File(path, id + ".jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            Log.v(TAG, "h - " + b.getHeight() + " w - " + b.getWidth());
            /*tmp = new Mat(b.getHeight(), b.getWidth(), CvType.CV_8UC1);
           Utils.bitmapToMat(b, tmp);
           //tmp = ih.drawSquares(boxes, tmp);

           b = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);*/
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //goToCaptureFormActivity
    public void goToCaptureFormActivity(View view) {
        Intent intent = new Intent(this, CaptureFormActivity.class);
        /*
        To Pass text/variables to new intent
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        */
        startActivity(intent);
    }
}

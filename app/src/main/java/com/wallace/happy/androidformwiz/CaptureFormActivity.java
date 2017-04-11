package com.wallace.happy.androidformwiz;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.plug.utils.FileManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static com.wallace.happy.androidformwiz.SelectWorkingFormActivity.FORM_REF;
import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class CaptureFormActivity extends AppCompatActivity {

    private DBHelper db = new DBHelper(this);
    String name;
    String idString;
    long id;
    String path;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/AndroidFormWiz/";

    private static final String TAG = "CaptureForm";
    private Context context;

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
        Log.d(TAG, "HERE - !!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_form);
        Intent intent = getIntent();
        id = getIntent().getLongExtra(FORM_REF, 0) + 1;

        if (!OpenCVLoader.initDebug()) {
            // Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            // Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        idString = String.valueOf(id);
        Log.d(TAG, "HERE - " +idString);

        FileManager manager = new FileManager(getApplicationContext());
        manager.writeRawToSD(FileManager.TESSERACT_PATH + "eng.traineddata",
                "eng.traineddata");
        //open camera
        openGallery();
    }


    /**
     *   Called when Image (selected from gallery or )from camera to display on screen
     */
    Bitmap bitmap;//chosen image
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private int PICK_IMAGE_REQUEST = 1;
    Uri photoURI;
    String mCurrentPhotoPath;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Log.d(TAG, "got Bitmap");
                String recognizedText="";
                List<String> result = processForm();
                for(int i = 0; i<result.size();i++)
                    recognizedText+=removeNewLine(result.get(i));
                insertText(recognizedText);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "no Bitmap");
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
                Log.d(TAG, "got Bitmap");
                String recognizedText="";
                List<String> result = processForm();
                for(int i = 0; i<result.size();i++)
                    recognizedText+=removeNewLine(result.get(i));
                insertText(recognizedText);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "no Bitmap");
                e.printStackTrace();
            }


        }
    }

    private String removeNewLine(String s) {
        return s.replace("\n", "").replace("\r", "") + "\n";
    }

    List<RotatedRect> getInflatedBoxesFromdB(double scaleFactorX, double scaleFactorY){
        //get details from DB
        Cursor c = db.getData(id);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex("Name"));
        path = c.getString(c.getColumnIndex("ImageSource"));
        int nBoxes = c.getInt(c.getColumnIndex("boxes"));
        return  db.getBigBoxes(id, nBoxes,scaleFactorX,scaleFactorY);
    }

    Size getSizeFromdB(){
        //get details from DB
        Cursor c = db.getData(id);
        c.moveToFirst();
        int x = c.getInt(c.getColumnIndex("X"));
        int y = c.getInt(c.getColumnIndex("Y"));
        return  new Size(x, y);
    }

    private ImageHelper ih = new ImageHelper();


    private List<String> processForm() {
        //convert to Mat
        Log.d(TAG, "page coord coming up");
        Mat tmp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CV_8UC1);
        Utils.bitmapToMat(bitmap, tmp);
        //find outer size!!! and crop
        RotatedRect rOutter = ih.findSquaure(tmp, bitmap.getWidth(), bitmap.getHeight());
        Log.d(TAG, "page coord " + rOutter.toString());
        tmp = ih.rotAndCrop(rOutter, tmp);

        //Get template bitmap size
        Size dsize = getSizeFromdB();
        Mat worker = new Mat((int)dsize.height, (int)dsize.width, CV_8UC1);
        //scale to same as original
        Imgproc.resize(tmp, worker, dsize);
        //get box info
        List<RotatedRect> boxes = getInflatedBoxesFromdB(1.5, 1.75);
        Log.d(TAG, "boxes" + boxes.get(0).toString());
        //Draw on
        //tmp = ih.drawSquares(boxes, tmp);
        //bitmap = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(tmp, bitmap);

        List<String> result = new ArrayList<>(boxes.size());

        //set up tess

        String lang = "eng";//for which the language data exists, usually "eng"
        tesseract.init(FileManager.STORAGE_PATH, lang);
        //only block caps ALIOBHIME
        tesseract.setVariable("tessedit_char_whitelist","ABCDEFGHIJKLMNOPQRSTUVXYZ");

        //get current view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_capture_form, null);
        LinearLayout myRoot = (LinearLayout) v.findViewById(R.id.root);

        //myRoot.addView(a);
         //for each box crop and feed to OCR
        for (int i = 0; i < boxes.size(); i++) {
            Log.d(TAG, "box - " + i + " -- " + boxes.get(i).toString());
            //crop big
            int h = (int)boxes.get(i).size.height;
            int w = (int)boxes.get(i).size.width;
            Mat curr = ih.rotAndCrop(boxes.get(i),worker);
            RotatedRect innerbox = ih.findSquaure(curr,w,h);
            Log.d(TAG, "inner " + innerbox.toString());
            //add if??
            if(innerbox.size.area()>0) {
                curr = ih.rotAndCrop(innerbox, curr);
            }
            //make grayscale
            Imgproc.cvtColor(curr, curr, Imgproc.COLOR_BGR2GRAY);
            //histogram
            // Compute histogram
            Vector<Mat> bgr_planes = new Vector<Mat>();
            Core.split(curr, bgr_planes);
            int bins = 256/4;
            MatOfInt histSize = new MatOfInt(bins);
            final MatOfFloat histRange = new MatOfFloat(0f, 256f);
            boolean accumulate = false;
            Mat b_hist = new  Mat();
            Imgproc.calcHist(bgr_planes, new MatOfInt(0),new Mat(), b_hist, histSize, histRange, accumulate);
            Log.v(TAG,"hist size" + b_hist.size().toString());
            //display hist
            Mat m = new Mat(b_hist.size(),CV_8UC1);
            Scalar colour = new Scalar(0,0,255);
            Point pt1 = new Point();
            Point pt2 = new Point();
            // Draw the histograms for B, G and R
            int hist_w = 512;
            int hist_h = 400;

            int bin_w =  hist_w/bins ;
            Scalar black = new Scalar( 0,0,0);
            Mat histImage = new Mat( hist_h, hist_w, CV_8UC3, black );

            /// Normalize the result to [ 0, histImage.rows ]
            Core.normalize(b_hist, b_hist, 0, histImage.rows(), NORM_MINMAX, -1);

            /// Draw the histogram
            Scalar red = new Scalar( 255, 0, 0);
            for( int index = 1; index < bins; index++ )
            {
                int a  = (int)b_hist.get(index-1,0)[0];
                int b  = (int)b_hist.get(index,0)[0];
                if(index < bins-1) {
                    int c = (int) b_hist.get(index + 1, 0)[0];
                    if(a<b &&b>c){

                        Log.v(TAG, "peak" + index + " - " + b);
                    }

                }
                pt1 = new Point(bin_w*(index-1),hist_h - a);
                pt2 = new Point(bin_w*(index),hist_h - b);

                Imgproc.line( histImage, pt1 ,pt2, red, 2, 8, 0  );


            }
            //image of hist
            Bitmap bmp2 = null;
            bmp2 = Bitmap.createBitmap(histImage.cols(), histImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(histImage, bmp2);//'CV_8UC1', 'CV_8UC3' or 'CV_8UC4'.
            ImageView hs = new ImageView(this);
            hs.setImageBitmap(bmp2);
            myRoot.addView(hs);

            //binary

            //convert to bit
            Bitmap currBit = Bitmap.createBitmap(curr.cols(), curr.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(curr, currBit);

            //read
            String found = readOCR(currBit);
            Log.d(TAG, i + " - " + found);
            result.add(i, found);
            //Add to screen
            // Add text
            TextView tv = new TextView(this);
            tv.setText(found);
            myRoot.addView(tv);
            //Add image
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(currBit);
            myRoot.addView(iv);

        }
        tesseract.end();
        // Display the view
        setContentView(v);
        return result;
    }

    TessBaseAPI tesseract = new TessBaseAPI();
    String readOCR(Bitmap bitmap){

        tesseract.setImage(bitmap);
        String recognizedText = tesseract.getUTF8Text();


        return recognizedText;
    }

    protected void insertText(String text){
        TextView nameTextView = (TextView)findViewById(R.id.textView);
        nameTextView.setText(text);
    }

    /**
     *  Called when the user clicks the camera button
     * Opens Camera so they can capture an image
     * */
    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) { }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.wallace.happy.androidformwiz.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    /**
     *  Called when the user clicks the gallery button
     * Opens Gallery so they can select an image
     * */
    public void openGallery() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
}

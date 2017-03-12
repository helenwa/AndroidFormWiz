package com.wallace.happy.androidformwiz;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.plug.utils.FileManager;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static com.wallace.happy.androidformwiz.SelectWorkingFormActivity.FORM_REF;

public class CaptureFormActivity extends AppCompatActivity {

    private DBHelper db = new DBHelper(this);
    String name;
    String idString;
    long id;
    String path;
    private int PICK_IMAGE_REQUEST = 1;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/AndroidFormWiz/";

    private static final String TAG = "SimpleAndroidOCR.java";
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_form);
        Intent intent = getIntent();
        id = getIntent().getLongExtra(FORM_REF, 0) + 1;
        idString = String.valueOf(id);

        FileManager manager = new FileManager(getApplicationContext());
        manager.writeRawToSD(FileManager.TESSERACT_PATH + "eng.traineddata",
                "eng.traineddata");
        //open camera
        openCamera();
    }


    /**
     *   Called when Image (selected from gallery or )from camera to display on screen
     */
    Bitmap bitmap;//chosen image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
            String recognizedText="";
            List<String> result = processForm(bitmap);
            for(int i = 0; i<result.size();i++)
                recognizedText+=result.get(i);
            insertText(recognizedText);
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
            String recognizedText = readOCR(bitmap);
            insertText(recognizedText);
        }
    }

    List<RotatedRect> getInflatedBoxesFromdB(double scaleFactor){
        //get details from DB
        Cursor c = db.getData(id);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex("Name"));
        path = c.getString(c.getColumnIndex("ImageSource"));
        int nBoxes = c.getInt(c.getColumnIndex("boxes"));
        return  db.getBigBoxes(id, nBoxes,scaleFactor);
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

    private List<String> processForm(Bitmap bitmap) {
        //convert to Mat
        Mat tmp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, tmp);
        //Get template bitmap size
        Size dsize = getSizeFromdB();
        Mat worker = new Mat((int)dsize.height, (int)dsize.width, CvType.CV_8UC1);
        //scale to same as original
        Imgproc.resize(tmp, worker, dsize);
        //get box info
        List<RotatedRect> boxes = getInflatedBoxesFromdB(1.5);
        List<String> result = new ArrayList<>(boxes.size());
         //for each box crop and feed to OCR
        for (int i = 0; i < boxes.size(); i++) {
            //crop big
            int h = (int)boxes.get(i).size.height;
            int w = (int)boxes.get(i).size.width;
            tmp = ih.rotAndCrop(boxes.get(i),worker);
            RotatedRect innerbox = ih.findSquaure(tmp,w,h);
            tmp = ih.rotAndCrop(innerbox,tmp);

            //read
            result.add(i,readOCR(Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888)));
        }
        return result;
    }


    String readOCR(Bitmap bitmap){
        TessBaseAPI tesseract = new TessBaseAPI();
        String lang = "eng";//for which the language data exists, usually "eng"
        tesseract.init(FileManager.STORAGE_PATH, lang);
        tesseract.setImage(bitmap);
        String recognizedText = tesseract.getUTF8Text();
        tesseract.end();

        return recognizedText;
    }
    protected void insertText(String text){
        TextView nameTextView = (TextView)findViewById(R.id.textView);
        nameTextView.setText(text);
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     *  Called when the user clicks the camera button
     * Opens Camera so they can capture an image
     * */
    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}

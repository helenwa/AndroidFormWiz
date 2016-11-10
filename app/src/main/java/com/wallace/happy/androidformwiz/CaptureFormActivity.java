package com.wallace.happy.androidformwiz;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
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
import java.net.URL;
import com.plug.utils.FileManager;

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
           readOCR();
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
            readOCR();
        }
    }


    protected void readOCR(){
        TessBaseAPI tesseract = new TessBaseAPI();

        String lang = "eng";//for which the language data exists, usually "eng"
        tesseract.init(FileManager.STORAGE_PATH, lang);
        tesseract.setImage(bitmap);
        String recognizedText = tesseract.getUTF8Text();
        tesseract.end();
        insertText(recognizedText);
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

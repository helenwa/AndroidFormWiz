package com.wallace.happy.androidformwiz;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SelectFormTemplateActivity extends AppCompatActivity {
    public final static String TEMP_REF = "com.wallace.happy.androidformwiz.TEMP_REF";

    private int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_form_template);
    }


    /**
     *  Called when the user clicks the gallery button
     * Opens Gallery so they can select an image
     * */
    public void openGallery(View view) {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
    *   Called when Image selected from gallery or from camera to display on screen
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

        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    /**
     *  Called when the user clicks the camera button
     * Opens Camera so they can capture an image
     * */
    public void openCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    public void processImageScreen(View view) {
        if(bitmap!=null) {
            Intent intent = new Intent(this, ProcessImageTemplateActivity.class);
            //*      To Pass text/variables to new intent
            String imagePath = saveToInternalStorage(bitmap);
            intent.putExtra(TEMP_REF, imagePath);
            //intent.putExtra("Image", bitmap);
            startActivity(intent);
        }
        else {
            //todo show warning
        }
    }

    //saves image to file
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath=new File(directory,"newTemplate.jpg");

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

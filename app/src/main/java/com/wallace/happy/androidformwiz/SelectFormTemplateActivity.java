package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

import static android.R.attr.data;

public class SelectFormTemplateActivity extends AppCompatActivity {

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
            intent.putExtra("Image", bitmap);
            startActivity(intent);
        }
        else {
            //todo show warning
        }
    }







}

package com.wallace.happy.androidformwiz;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import static android.R.attr.bitmap;

public class ProcessImageTemplateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image_template);
        Bitmap bitmap = (Bitmap) getIntent().getParcelableExtra("Image");
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
        //todo image processing
    }
}

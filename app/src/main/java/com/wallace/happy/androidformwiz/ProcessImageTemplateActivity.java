package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.wallace.happy.androidformwiz.SelectFormTemplateActivity.TEMP_REF;


public class ProcessImageTemplateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image_template);
        Intent intent = getIntent();

        String templateReference = intent.getStringExtra(TEMP_REF);
        loadImageFromStorageTOScreen(templateReference);

    }

    Bitmap b;
    String p;
    //loads image from file to screen
    private void loadImageFromStorageTOScreen(String path)
    {
        try {
            p=path;
            File f=new File(path, "newTemplate.jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img=(ImageView)findViewById(R.id.imageView);
            img.setImageBitmap(b);
            //p=
            processImage(b);
            //editFormTemplateScreen();
        }
            catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }
    private String processImage(Bitmap b){
        //TODO
        Handler mHandler = new Handler();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                editFormTemplateScreen();
            }
        }, 2000);
        return "todo";
    }
    public void editFormTemplateScreen() {
        if(b!=null) {
            Intent intent = new Intent(this, EditFormTemplateActivity.class);
            //*      To Pass text/variables to new intent
            intent.putExtra(TEMP_REF, p);
            startActivity(intent);
        }
        else {
            //todo show warning
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    //static {
      //  System.loadLibrary("native-lib");
    //}
}

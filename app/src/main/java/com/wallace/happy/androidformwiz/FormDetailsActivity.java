package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.wallace.happy.androidformwiz.SelectFormTemplateActivity.TEMP_REF;
import static com.wallace.happy.androidformwiz.SelectWorkingFormActivity.FORM_REF;

public class FormDetailsActivity extends AppCompatActivity {

    private DBHelper db = new DBHelper(this);
    String name;
    String idString;
    String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_details);
        Intent intent = getIntent();
        long id = getIntent().getLongExtra(FORM_REF, 0) + 1;
        String idString = String.valueOf(id);

        //get details from DB
        Cursor c = db.getData(id);
        c.moveToFirst();
        name = c.getString(c.getColumnIndex("Name"));
        path = c.getString(c.getColumnIndex("ImageSource"));
        loadImageFromStorageTOScreen(path, idString);
        TextView nameTextView = (TextView)findViewById(R.id.nameTextView);
        nameTextView.setText(name + " : "+idString + "  :  " + path);
    }

    Bitmap b;
    String p;
    //loads image from file to screen
    private void loadImageFromStorageTOScreen(String path, String id)
    {
        try {
            p=path;
            File f=new File(path, id + ".jpg");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img=(ImageView)findViewById(R.id.imageView);
            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }
}

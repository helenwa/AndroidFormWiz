package com.wallace.happy.androidformwiz;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.wallace.happy.androidformwiz.SelectFormTemplateActivity.TEMP_REF;

public class EditFormTemplateActivity extends AppCompatActivity {

    public String templateReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_form_template);
        //load image
        Intent intent = getIntent();
        templateReference = intent.getStringExtra(TEMP_REF);
        loadImageFromStorageTOScreen(templateReference);
        //todo image processing



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
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }


    private DBHelper db = new DBHelper(this);

    public void saveForm(View view){
        /*
        SQLiteDat abase formDB = openOrCreateDatabase("formDB",MODE_PRIVATE,null);
        formDB.execSQL("CREATE TABLE IF NOT EXISTS Forms(ID int NOT NULL AUTO_INCREMENT,Name VARCHAR,ImageSource VARCHAR,PRIMARY KEY (ID));");
        formDB.execSQL("INSERT INTO Forms(Name,ImageSource) VALUES('"+ nameString + "','" + templateReference + "');");
        */
        EditText mEdit = (EditText)findViewById(R.id.editText);
        String nameString = mEdit.getText().toString();
        String id = db.insertForm  (nameString, templateReference);
        saveToInternalStorage(b, id);
        //TODO save box variables in second table
    }


    //saves image to file
    private String saveToInternalStorage(Bitmap bitmapImage, String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath=new File(directory,name);//"newTemplate.jpg");

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

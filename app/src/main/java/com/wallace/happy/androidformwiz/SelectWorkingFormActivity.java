package com.wallace.happy.androidformwiz;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SelectWorkingFormActivity extends AppCompatActivity {

    private DBHelper db = new DBHelper(this);
    ArrayList<String> forms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_working_form);

        forms = db.getAllForms();

        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.list_view,forms);

        ListView mList = (ListView)findViewById(R.id.form_list);
        mList.setAdapter(adapter);

    }
}

package com.wallace.happy.androidformwiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SelectWorkingFormActivity extends AppCompatActivity {

    public final static String FORM_REF = "com.wallace.happy.androidformwiz.FORM_REF";
    private DBHelper db = new DBHelper(this);
    ArrayList<String> forms;
    ListView mList;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_working_form);

        forms = db.getAllForms();
        adapter = new ArrayAdapter<String>(this,R.layout.list_view,forms);

        mList = (ListView)findViewById(R.id.form_list);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(mMessageClickedHandler);

    }

    // Create a message handling object as an anonymous class.
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            Intent intent = new Intent(SelectWorkingFormActivity.this, FormDetailsActivity.class);
            //*      To Pass id to new intent
            intent.putExtra(FORM_REF, id);
            startActivity(intent);
        }

    };


}

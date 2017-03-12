package com.wallace.happy.androidformwiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private ImageHelper ih = new ImageHelper();
    private static final String TAG = "HOME";

    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(ih.test());

        Log.v(TAG, "TESTLOG");

    }

    /** Called when the user clicks the New Form button */
    public void goToNewFormActivity(View view) {
        Intent intent = new Intent(this, SelectFormTemplateActivity.class);
/*      To Pass text/variables to new intent
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);*/
        startActivity(intent);
    }
    /** Called when the user clicks the existing Form button */
    public void goToFormActivity(View view) {
        Intent intent = new Intent(this, SelectWorkingFormActivity.class);
        startActivity(intent);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}

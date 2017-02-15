package com.wallace.happy.androidformwiz;
 import java.util.ArrayList;
        import java.util.HashMap;
 import java.util.List;
 import java.util.Objects;

 import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.DatabaseUtils;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.database.sqlite.SQLiteDatabase;

 import org.opencv.core.MatOfPoint;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "form.db";
    public static final String FORM_TABLE_NAME = "Forms";
    public static final String FORM_COLUMN_ID = "ID";
    public static final String FORM_COLUMN_NAME = "Name";
    public static final String FORM_COLUMN_IMAGESOURCE = "ImageSource";
    public static final String SQUARE_TABLE_NAME = "Square";

    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + FORM_TABLE_NAME + "(" +
                        "id INTEGER PRIMARY KEY," +
                        "Name VARCHAR," +
                        "ImageSource VARCHAR);"
        );
        db.execSQL(
                "create table " + SQUARE_TABLE_NAME +"(" +
                        "id INTEGER PRIMARY KEY, " +
                        "formId INTEGER," +
                        "pt0 INTEGER, " +
                        "pt1 INTEGER, " +
                        "pt2 INTEGER, " +
                        "pt3 INTEGER," +
                        "FOREIGN KEY(formId) REFERENCES "+ FORM_TABLE_NAME +"(id)); "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS forms");
        onCreate(db);
    }

    public String insertForm(String name, String imageSource, List<MatOfPoint> squares )
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FORM_COLUMN_NAME, name);
        contentValues.put(FORM_COLUMN_IMAGESOURCE, imageSource);
        long idLong = db.insert(FORM_TABLE_NAME, null, contentValues);
        return String.valueOf(idLong);
    }

    public Cursor getData(Long id){//was int
        SQLiteDatabase db = this.getReadableDatabase();
        return  db.rawQuery( "select * from Forms where id="+id+"", null );
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, FORM_TABLE_NAME);
    }

    public boolean updateForm (Integer id, String name, String imageSource)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", name);
        contentValues.put("ImageSource", imageSource);
        db.update("Forms", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteForm (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Forms",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllForms()
    {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from Forms", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(FORM_COLUMN_NAME)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}

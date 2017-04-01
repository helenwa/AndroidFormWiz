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
 import org.opencv.core.RotatedRect;
 import org.opencv.core.Size;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "form.db";
    public static final String FORM_TABLE_NAME = "Forms";
    public static final String FORM_COLUMN_ID = "ID";
    public static final String FORM_COLUMN_NAME = "Name";
    public static final String FORM_COLUMN_IMAGESOURCE = "ImageSource";
    public static final String SQUARE_TABLE_NAME = "Square";

    private ImageHelper ih = new ImageHelper();
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
                        "ImageSource VARCHAR," +
                        "X REAL," +
                        "Y REAL," +
                        "boxes INTEGER" +

                        ");"
        );
        db.execSQL(
                "create table " + SQUARE_TABLE_NAME +"(" +
                        "id INTEGER PRIMARY KEY, " +
                        "formId INTEGER," +
                        "pt0 REAL, " +
                        "pt1 REAL, " +
                        "pt2 REAL, " +
                        "pt3 REAL," +
                        "pt4 REAL," +

                        "FOREIGN KEY(formId) REFERENCES "+ FORM_TABLE_NAME +"(id)); "
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS forms");
        onCreate(db);
    }

    public String insertForm(String name, String imageSource, List<RotatedRect> squares, double w, double h)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FORM_COLUMN_NAME, name);
        contentValues.put(FORM_COLUMN_IMAGESOURCE, imageSource);
        contentValues.put("boxes", squares.size());
        contentValues.put("X", w);
        contentValues.put("Y", h);
        long idLong = db.insert(FORM_TABLE_NAME, null, contentValues);
        for(int i=0;i<squares.size();i++){
            double rect[] = ih.toArray(squares.get(i));
            ContentValues square = new ContentValues();
            for(int j=0; j<5;j++) {
                square.put("pt" + j , rect[j]);
            }
            square.put("formId", idLong);
            long id = db.insert(SQUARE_TABLE_NAME, null, square);
        }


        return String.valueOf(idLong);
    }
    public List<RotatedRect> getBoxes(Long id, int b){
        List<RotatedRect> r = new ArrayList<>(b);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + SQUARE_TABLE_NAME + " where formId="+id+"", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            double rect[] = new double[5];
            for(int i=0;i<5;i++) {
                rect[i] = res.getDouble(res.getColumnIndex("pt"+i));
            }
            r.add(new RotatedRect(rect));
            res.moveToNext();
        }
        res.close();
        return r;
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

    public List<RotatedRect> getBigBoxes(long id, int nBoxes, double scaleFactorX, double scaleFactorY){
        List<RotatedRect> r = new ArrayList<>(nBoxes);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + SQUARE_TABLE_NAME + " where formId="+id+"", null );
        res.moveToFirst();
        while(!res.isAfterLast()){
            double rect[] = new double[5];
            for(int i=0;i<5;i++) {
                rect[i] = res.getDouble(res.getColumnIndex("pt"+i));
            }
            rect[2]=rect[2]*scaleFactorX;
            rect[3]=rect[3]*scaleFactorY;
            r.add(new RotatedRect(rect));
            res.moveToNext();
        }
        res.close();
        return r;
    }
}

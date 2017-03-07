package model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by MOlawale on 6/11/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "Table Operations";
    SQLiteDatabase database;
    public DatabaseHelper(Context ct) {

        super(ct, DbInfo.DATABASE_NAME, null, DbInfo.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(DbInfo.CREATE_TABLE);
             db.execSQL(DbInfo.CREATE_SECOND_TABLE);
        } catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXIST " + DbInfo.USER_TABLE);
            db.execSQL("DROP TABLE IF EXIST " + DbInfo.GRADE_TABLE);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

}
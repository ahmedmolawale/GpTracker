package com.ahmedmolawale.gradepointmanager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import model.DatabaseHelper;
import model.DbInfo;


/**
 * Created by MOlawale on 7/6/2015.
 */
public class GpmContentProvider extends ContentProvider {

    private DatabaseHelper databaseHelper;
    private static final String AUTHORITY = "com.app.gpm";
    private static final String USERS_PATH = "users";   //for the users table
    private static final String GRADE_PATH = "grades";  //for the grades table
    public static final int USER = 1;   //this and the below one are used to identify when the client specify a record to query or delete or update
    public static final int USER_ID = 2;
    public static final int GRADE = 3;
    public static final int GRADE_ID = 4;
    public static final Uri CONTENT_URI_USERS = Uri.parse("content://" + AUTHORITY + "/" + USERS_PATH);
    public static final Uri CONTENT_URI_GRADES = Uri.parse("content://" + AUTHORITY + "/" + GRADE_PATH);

    private static UriMatcher uriMatcher
            = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, USERS_PATH, USER);
        uriMatcher.addURI(AUTHORITY, USERS_PATH + "/#",
                USER_ID);
        //for the grades table now
        uriMatcher.addURI(AUTHORITY, GRADE_PATH, GRADE);
        uriMatcher.addURI(AUTHORITY, GRADE_PATH + "/#", GRADE_ID);

    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();

        long _id = 0;
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case USER:
                _id = sqlDB.insert(DbInfo.USER_TABLE,
                        null, values);
                break;
            case GRADE:
                _id = sqlDB.insert(DbInfo.GRADE_TABLE,
                        null, values);
                break;
            default:
                throw new IllegalArgumentException("Failed to insert row into " + uri);


        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, _id);


    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        Cursor cursor;

        //now, we check the uri if the client specify a particular row to be fecteched in it, if so, we
        //we set the where clause of the query builder. Lets see how its done

        int uriType = uriMatcher.match(uri);
        switch (uriType) {

            case USER:
                queryBuilder.setTables(DbInfo.USER_TABLE);
                break;
            case USER_ID:
                queryBuilder.setTables(DbInfo.USER_TABLE);
                queryBuilder.appendWhere(DbInfo.ID + "=" + uri.getLastPathSegment());
                break;
            case GRADE:
                queryBuilder.setTables(DbInfo.GRADE_TABLE);
                break;
            case GRADE_ID:
                queryBuilder.setTables(DbInfo.GRADE_TABLE);
                queryBuilder.appendWhere(DbInfo.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " +
                        uri);
        }

        cursor = queryBuilder.query(databaseHelper.getWritableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case USER:
                rowsUpdated =
                        sqlDB.update(DbInfo.USER_TABLE,
                                values,
                                selection,
                                selectionArgs);
                break;
            case USER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(DbInfo.USER_TABLE,
                                    values,
                                    DbInfo.ID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(DbInfo.USER_TABLE,
                                    values,
                                    DbInfo.ID + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            case GRADE:
                rowsUpdated =
                        sqlDB.update(DbInfo.GRADE_TABLE,
                                values,
                                selection,
                                selectionArgs);
                break;
            case GRADE_ID:
                String _id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(DbInfo.GRADE_TABLE,
                                    values,
                                    DbInfo.ID + "=" + _id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(DbInfo.GRADE_TABLE,
                                    values,
                                    DbInfo.ID + "=" + _id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " +
                        uri);
        }
        getContext().getContentResolver().notifyChange(uri,
                null);
        return rowsUpdated;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case USER:
                rowsDeleted = sqlDB.delete(DbInfo.USER_TABLE,
                        selection,
                        selectionArgs);
                break;

            case USER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DbInfo.USER_TABLE,
                            DbInfo.ID + "=" + id,
                            null);
                } else {
                    //is this ever gonna be possible when the id actually is unique; the selection criteria woudnlt count but its kinda good we test it also sha
                    rowsDeleted = sqlDB.delete(DbInfo.USER_TABLE,
                            DbInfo.ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            case GRADE:
                rowsDeleted = sqlDB.delete(DbInfo.GRADE_TABLE,
                        selection,
                        selectionArgs);
                break;

            case GRADE_ID:
                String _id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DbInfo.GRADE_TABLE,
                            DbInfo.ID + "=" + _id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DbInfo.GRADE_TABLE,
                            DbInfo.ID + "=" + _id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }
}

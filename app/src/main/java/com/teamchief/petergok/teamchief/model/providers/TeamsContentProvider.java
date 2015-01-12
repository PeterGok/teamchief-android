package com.teamchief.petergok.teamchief.model.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.teamchief.petergok.teamchief.model.MySQLiteHelper;
import com.teamchief.petergok.teamchief.model.tables.TeamsTable;

import java.util.Arrays;
import java.util.HashSet;

import javax.xml.transform.Templates;

/**
 * Created by Peter on 2015-01-11.
 */
public class TeamsContentProvider extends ContentProvider {
    private static HashSet<String> AVAILABLE_COLUMNS = new HashSet<>(Arrays.asList(TeamsTable.getFullProjection()));
    // database
    private MySQLiteHelper database;

    // used for the UriMacher
    private static final int TEAMS = 11;
    private static final int TEAM_ID = 21;

    private static final String AUTHORITY = "com.teamchief.petergok.teamchief.teamscontentprovider";

    private static final String BASE_PATH = "teams";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/teams";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/team";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TEAMS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TEAM_ID);
    }

    @Override
    public boolean onCreate() {
        database = new MySQLiteHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(TeamsTable.TABLE_TEAMS);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TEAMS:
                break;
            case TEAM_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(TeamsTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case TEAMS:
                id = sqlDB.insert(TeamsTable.TABLE_TEAMS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case TEAMS:
                rowsDeleted = sqlDB.delete(TeamsTable.TABLE_TEAMS, selection,
                        selectionArgs);
                break;
            case TEAM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(TeamsTable.TABLE_TEAMS,
                            TeamsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(TeamsTable.TABLE_TEAMS,
                            TeamsTable.COLUMN_ID + "=" + id
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

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case TEAMS:
                rowsUpdated = sqlDB.update(TeamsTable.TABLE_TEAMS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TEAM_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(TeamsTable.TABLE_TEAMS,
                            values,
                            TeamsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(TeamsTable.TABLE_TEAMS,
                            values,
                            TeamsTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            // check if all columns which are requested are available
            if (!AVAILABLE_COLUMNS.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}

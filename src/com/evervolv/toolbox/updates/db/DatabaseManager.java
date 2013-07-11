package com.evervolv.toolbox.updates.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    /* Version */
    private static final int DATABASE_VERSION        = 1;
    private static final String DATABASE_NAME        = "updates.db";
    /* Table types (mapped to names) */
    public static final int NIGHTLIES                = 0;
    public static final int RELEASES                 = 1;
    public static final int TESTING                  = 2;
    public static final int GAPPS                    = 3;
    public static final int DOWNLOADS                = 4;
    /* Table names */
    private static final String NIGHTLIES_TABLE_NAME = "nightlies";
    private static final String RELEASES_TABLE_NAME  = "releases";
    private static final String TESTING_TABLE_NAME   = "testing";
    private static final String GAPPS_TABLE_NAME     = "gapps";
    private static final String DOWNLOADS_TABLE_NAME = "downloads";
    /* Fields */
    private static final String COLUMN_ID            = "_id";
    private static final String COLUMN_DATE          = "date";
    private static final String COLUMN_NAME          = "name";
    private static final String COLUMN_MD5SUM        = "md5sum";
    private static final String COLUMN_LOCATION      = "location";
    private static final String COLUMN_DEVICE        = "device";
    private static final String COLUMN_MESSAGE       = "message";
    private static final String COLUMN_TYPE          = "type";
    private static final String COLUMN_SIZE          = "size";
    private static final String COLUMN_COUNT         = "count";
    private static final String COLUMN_DOWNLOAD_ID   = "download_id";
    private static final String COLUMN_LOCATION_URI  = "location_uri";

    private static final String[] ALL_MANIFEST_COLUMNS = {
            COLUMN_ID,
            COLUMN_DATE,
            COLUMN_NAME,
            COLUMN_MD5SUM,
            COLUMN_LOCATION,
            COLUMN_DEVICE,
            COLUMN_MESSAGE,
            COLUMN_TYPE,
            COLUMN_SIZE,
            COLUMN_COUNT,
    };

    private static final String[] ALL_DOWNLOADS_COLUMNS = {
            COLUMN_ID,
            COLUMN_DOWNLOAD_ID,
            COLUMN_MD5SUM,
            COLUMN_LOCATION_URI,
    };

    private static final String MANIFEST_TABLE_TEMPLATE = " (" +
            COLUMN_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATE     + " TEXT, " +
            COLUMN_NAME     + " TEXT, " +
            COLUMN_MD5SUM   + " TEXT, " +
            COLUMN_LOCATION + " TEXT, " +
            COLUMN_DEVICE   + " TEXT, " +
            COLUMN_MESSAGE  + " TEXT, " +
            COLUMN_TYPE     + " TEXT, " +
            COLUMN_SIZE     + " INT, " +
            COLUMN_COUNT    + " INT);";

    private static final String DOWNLOADS_TABLE_TEMPLATE = " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DOWNLOAD_ID + " INTEGER, " +
            COLUMN_MD5SUM      + " TEXT, " +
            COLUMN_LOCATION_URI+ " TEXT);";


    private final Context context;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NIGHTLIES_TABLE_NAME + MANIFEST_TABLE_TEMPLATE);
            db.execSQL("CREATE TABLE " + RELEASES_TABLE_NAME + MANIFEST_TABLE_TEMPLATE);
            db.execSQL("CREATE TABLE " + TESTING_TABLE_NAME + MANIFEST_TABLE_TEMPLATE);
            db.execSQL("CREATE TABLE " + GAPPS_TABLE_NAME + MANIFEST_TABLE_TEMPLATE);
            db.execSQL("CREATE TABLE " + DOWNLOADS_TABLE_NAME + DOWNLOADS_TABLE_TEMPLATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO
        }

    }

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public DatabaseManager open() throws SQLiteException {
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        databaseHelper.close();
    }

    /* NIGHTLY, RELEASE, TESTING, GAPPS Tables */

    public void updateManifest(int table, JSONArray entries) throws SQLiteException, JSONException {
        String TABLE_NAME = getTable(table);
        database.delete(TABLE_NAME,null,null); // Kill em all
        for (int i=0; i<entries.length();i++) {
            JSONObject entry = entries.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE,     entry.getString(COLUMN_DATE));
            values.put(COLUMN_NAME,     entry.getString(COLUMN_NAME));
            values.put(COLUMN_MD5SUM,   entry.getString(COLUMN_MD5SUM));
            values.put(COLUMN_LOCATION, entry.getString(COLUMN_LOCATION));
            values.put(COLUMN_DEVICE,   entry.getString(COLUMN_DEVICE));
            values.put(COLUMN_MESSAGE,  entry.getString(COLUMN_MESSAGE));
            values.put(COLUMN_TYPE,     entry.getString(COLUMN_TYPE));
            values.put(COLUMN_SIZE,     entry.getInt(COLUMN_SIZE));
            values.put(COLUMN_COUNT,    entry.getInt(COLUMN_COUNT));
            database.insert(TABLE_NAME, null, values);
        }
    }

    public List<ManifestEntry> fetchManifest(int table) throws SQLiteException {
        String TABLE_NAME = getTable(table);
        List<ManifestEntry> entries = new ArrayList<ManifestEntry>();
        Cursor c = database.query(TABLE_NAME, ALL_MANIFEST_COLUMNS,
                null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ManifestEntry e = cursorToManifestEntry(c);
            entries.add(e);
            c.moveToNext();
        }
        c.close();
        return entries;
    }

    /* DOWNLOADS Table */

    public void addDownload(DownloadEntry entry) throws SQLiteException {
        //TODO check and remove duplicates
        ContentValues values = new ContentValues();
        values.put(COLUMN_DOWNLOAD_ID,  entry.getDownloadId());
        values.put(COLUMN_MD5SUM,       entry.getMd5sum());
        values.put(COLUMN_LOCATION_URI, entry.getLocationUri());
        database.insert(DOWNLOADS_TABLE_NAME, null, values);
    }

    public long queryDownloads(String md5sum) throws SQLiteException {
        Cursor c = database.query(DOWNLOADS_TABLE_NAME, ALL_DOWNLOADS_COLUMNS,
                null,null,null,null,null);
        long id = -1;

        /* Reverse lookup, in case of duplicates
           we want the newest one. */
        c.moveToLast();
        while (!c.isBeforeFirst()) {
            DownloadEntry e = cursorToDownloadEntry(c);
            if (e.getMd5sum().equals(md5sum)) {
                id = e.getDownloadId();
                break;
            }
            c.moveToPrevious();
        }
        c.close();
        return id;
    }

    public boolean queryDownloads(long downloadId) {
        Cursor c = database.query(DOWNLOADS_TABLE_NAME, ALL_DOWNLOADS_COLUMNS,
                null,null,null,null,null);
        boolean exists = false;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            DownloadEntry e = cursorToDownloadEntry(c);
            if (e.getDownloadId() == downloadId) {
                exists = true;
                break;
            }
            c.moveToNext();
        }
        c.close();
        return exists;
    }

    public void removeDownload(long downloadId) throws SQLiteException {
        database.delete(DOWNLOADS_TABLE_NAME,
                COLUMN_DOWNLOAD_ID + "=" + downloadId, null);
    }

    /* Helpers */

    private String getTable(int table) {
        String name = null;
        switch (table) {
            case DOWNLOADS:
                name = DOWNLOADS_TABLE_NAME;break;
            case TESTING:
                name = TESTING_TABLE_NAME;break;
            case RELEASES:
                name = RELEASES_TABLE_NAME;break;
            case NIGHTLIES:
                name = NIGHTLIES_TABLE_NAME;break;
            case GAPPS:
                name = GAPPS_TABLE_NAME;break;
        }
        return name;
    }

    private ManifestEntry cursorToManifestEntry(Cursor cursor) {
        ManifestEntry entry = new ManifestEntry();
        entry.setId(cursor.getLong(0));
        entry.setDate(cursor.getString(1));
        entry.setName(cursor.getString(2));
        entry.setMd5sum(cursor.getString(3));
        entry.setLocation(cursor.getString(4));
        entry.setDevice(cursor.getString(5));
        entry.setMessage(cursor.getString(6));
        entry.setType(cursor.getString(7));
        entry.setSize(cursor.getInt(8));
        entry.setCount(cursor.getInt(9));
        return entry;
    }

    private DownloadEntry cursorToDownloadEntry(Cursor cursor) {
        DownloadEntry entry = new DownloadEntry();
        entry.setId(cursor.getLong(0));
        entry.setDownloadId(cursor.getLong(1));
        entry.setMd5sum(cursor.getString(2));
        entry.setLocationUri(cursor.getString(3));
        return entry;
    }
}

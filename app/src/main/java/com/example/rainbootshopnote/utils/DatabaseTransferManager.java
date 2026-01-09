package com.example.rainbootshopnote.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DatabaseTransferManager {

    private DatabaseTransferManager() {}

    public static File getInternalDbFile(Context context, String dbName) {
        return context.getDatabasePath(dbName);
    }

    public static Uri exportToDownloads(Context context, String dbName) throws Exception {
        File dbFile = getInternalDbFile(context, dbName);
        if (!dbFile.exists()) throw new IllegalStateException("DB file not found: " + dbFile);

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String outName = dbName.replace(".db", "") + "_backup_" + ts + ".db";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, outName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IllegalStateException("Failed to create download item");

            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = resolver.openOutputStream(uri, "w")) {
                if (out == null) throw new IllegalStateException("OutputStream null");
                copy(in, out);
            }
            return uri;
        } else {
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) dir = context.getCacheDir();
            File outFile = new File(dir, outName);

            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = new FileOutputStream(outFile)) {
                copy(in, out);
            }
            return Uri.fromFile(outFile);
        }
    }

    public static void importFromUri(
            Context context,
            Uri uri,
            String dbName,
            @Nullable String[] requiredTables
    ) throws Exception {

        File dbFile = getInternalDbFile(context, dbName);

        File temp = new File(context.getCacheDir(), "import_temp.db");
        copyUriToFile(context, uri, temp);

        if (!isValidSqliteDb(temp)) {
            temp.delete();
            throw new IllegalArgumentException("Invalid sqlite database file");
        }

        if (requiredTables != null && requiredTables.length > 0) {
            if (!hasRequiredTables(temp, requiredTables)) {
                temp.delete();
                throw new IllegalArgumentException("Database schema mismatch");
            }
        }

        if (dbFile.exists()) {
            backupInternal(context, dbName);
        } else {
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
        }

        copyFile(temp, dbFile);
        temp.delete();
    }

    public static File backupInternal(Context context, String dbName) throws Exception {
        File dbFile = getInternalDbFile(context, dbName);
        if (!dbFile.exists()) return null;

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        File backup = new File(parent, dbName.replace(".db", "") + "_preimport_" + ts + ".db");
        copyFile(dbFile, backup);
        return backup;
    }

    private static boolean isValidSqliteDb(File file) {
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(
                file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY
        )) {
            return db.isOpen();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasRequiredTables(File file, String[] requiredTables) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            for (String t : requiredTables) {
                if (!tableExists(db, t)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    private static boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName}
            );
            return c.moveToFirst();
        } finally {
            if (c != null) c.close();
        }
    }

    private static void copyUriToFile(Context context, Uri uri, File outFile) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream in = resolver.openInputStream(uri);
             OutputStream out = new FileOutputStream(outFile)) {
            if (in == null) throw new IllegalStateException("InputStream null");
            copy(in, out);
        }
    }

    private static void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst, false)) {
            copy(in, out);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) != -1) {
            out.write(buf, 0, r);
        }
        out.flush();
    }
}

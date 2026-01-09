package com.example.rainbootshopnote.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class DatabaseManager {

    // DB dosya adı (DBHelper ile aynı olmalı)
    public static final String DB_NAME = "rainbootnote.db";

    // Beklenen tablolar (7 tabloya göre güncelle)
    private static final List<String> REQUIRED_TABLES = Arrays.asList(
            "users",
            "products",
            "sales",
            "deposits",
            "withdrawals",
            "admins",
            "insertProducts"
    );

    private DatabaseManager() {}

    /** Internal DB dosya yolu */
    public static File getInternalDbFile(Context context) {
        return context.getDatabasePath(DB_NAME);
    }

    /** DB klasörünü garanti et */
    private static void ensureDbDir(Context context) {
        File dbFile = getInternalDbFile(context);
        File dir = dbFile.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
    }

    /** Mevcut DB'yi timestamp ile yedekle */
    public static File backupExistingDb(Context context) throws IOException {
        File dbFile = getInternalDbFile(context);
        if (!dbFile.exists()) return null;

        ensureDbDir(context);

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File backup = new File(dbFile.getParentFile(), DB_NAME.replace(".db","") + "_backup_" + ts + ".db");

        copyFile(dbFile, backup);
        return backup;
    }

    /** Dışarıdan seçilen DB'yi uygula: doğrula -> yedekle -> replace */
    public static void importDbFromUri(Context context, Uri uri) throws Exception {
        ensureDbDir(context);

        // 1) Uri içeriğini temp dosyaya kopyala
        File temp = new File(context.getCacheDir(), "import_temp.db");
        copyUriToFile(context, uri, temp);

        // 2) Temp DB doğrula
        if (!isValidDatabase(temp.getAbsolutePath())) {
            temp.delete();
            throw new IllegalArgumentException("Seçilen dosya geçerli bir veritabanı değil veya tablolar uyuşmuyor.");
        }

        // 3) Mevcut DB yedeğini al
        backupExistingDb(context);

        // 4) Temp'i internal DB'nin üstüne yaz
        File target = getInternalDbFile(context);
        copyFile(temp, target);

        // 5) temp temizle
        temp.delete();
    }

    /** Tablolar var mı? */
    public static boolean isValidDatabase(String path) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

            // required tabloların varlığını kontrol et
            for (String t : REQUIRED_TABLES) {
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

    private static void copyUriToFile(Context context, Uri uri, File outFile) throws IOException {
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(outFile)) {

            if (in == null) throw new FileNotFoundException("Dosya açılamadı.");

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.flush();
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.flush();
        }
    }
}

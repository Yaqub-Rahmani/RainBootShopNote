package com.example.rainbootshopnote.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DB Import/Export tek noktada.
 *
 * 1) Local DB'yi değiştirme (import/replace)
 * 2) DB yedeğini telefona indirme (Downloads export)
 */
public final class DbImportExportManager {

    private static final String SQLITE_MIME = "application/x-sqlite3";
    private static final SimpleDateFormat TS_FORMAT =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private DbImportExportManager() {}

    /**
     * Downloads'a backup yazar.
     * - API 29+: MediaStore Downloads (izin gerektirmez)
     * - API 24-28: Public Downloads (WRITE_EXTERNAL_STORAGE gerekir)
     */
    public static Uri backupToDownloads(Context context, String dbName) throws Exception {
        String fileName = buildBackupFileName(dbName, null);
        return backupToDownloadsInternal(context, dbName, fileName);
    }

    /**
     * Kullanıcı adı ile backup yazar (dosya adında).
     */
    public static Uri backupToDownloads(Context context, String dbName, String userName) throws Exception {
        String fileName = buildBackupFileName(dbName, userName);
        return backupToDownloadsInternal(context, dbName, fileName);
    }

    /**
     * URI'den uygulamanın DB dosyasını değiştirir (replace).
     *
     * ÖNEMLİ:
     * - Bu method çağrılmadan önce DB bağlantısını kapatman iyi olur (DBHelper.close()).
     * - Import'tan sonra yeni DBHelper açabilirsin.
     */
    public static void importDatabaseFromUri(Context context, Uri sourceUri, String dbName) throws Exception {
        if (sourceUri == null) throw new IllegalArgumentException("sourceUri null");
        if (dbName == null) dbName = "rainboot.db";

        File targetFile = context.getDatabasePath(dbName);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }

        ContentResolver resolver = context.getContentResolver();

        // 1) Source URI -> temp
        File temp = new File(context.getCacheDir(), "import_tmp_" + System.currentTimeMillis() + ".db");
        try (InputStream in = resolver.openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(temp, false)) {

            if (in == null) throw new IllegalStateException("InputStream null");
            copy(in, out);
            out.flush();
        }

        if (!temp.exists() || temp.length() < 1024) {
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
            throw new IllegalStateException("Imported DB is too small or missing");
        }

        // 2) Eski DB'yi yedekle
        File backupOld = new File(targetFile.getAbsolutePath() + ".bak");
        if (targetFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            backupOld.delete();
            //noinspection ResultOfMethodCallIgnored
            targetFile.renameTo(backupOld);
        }

        // 3) Temp -> target
        boolean ok = temp.renameTo(targetFile);
        if (!ok) {
            try (InputStream in2 = new FileInputStream(temp);
                 OutputStream out2 = new FileOutputStream(targetFile, false)) {
                copy(in2, out2);
                out2.flush();
            }
            //noinspection ResultOfMethodCallIgnored
            temp.delete();
        }

        // 4) SQLite yardımcı dosyaları temizle (WAL/SHM)
        File wal = new File(targetFile.getAbsolutePath() + "-wal");
        File shm = new File(targetFile.getAbsolutePath() + "-shm");
        //noinspection ResultOfMethodCallIgnored
        wal.delete();
        //noinspection ResultOfMethodCallIgnored
        shm.delete();
    }

    // -----------------------------
    // Downloads Export (Internal)
    // -----------------------------

    private static Uri backupToDownloadsInternal(Context context, String dbName, String fileName) throws Exception {
        File dbFile = context.getDatabasePath(dbName);
        if (!dbFile.exists()) {
            throw new IllegalStateException("DB not found: " + dbFile.getAbsolutePath());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return backupToDownloadsApi29Plus(context, dbFile, fileName);
        } else {
            return backupToDownloadsLegacy(context, dbFile, fileName);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static Uri backupToDownloadsApi29Plus(Context context, File dbFile, String fileName) throws Exception {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, SQLITE_MIME);
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri itemUri = resolver.insert(collection, values);
        if (itemUri == null) throw new IllegalStateException("MediaStore insert failed");

        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = resolver.openOutputStream(itemUri, "w")) {

            if (out == null) throw new IllegalStateException("OutputStream null");
            copy(in, out);

        } catch (Exception e) {
            try { resolver.delete(itemUri, null, null); } catch (Exception ignore) {}
            throw e;
        }

        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(itemUri, values, null, null);

        return itemUri;
    }

    /**
     * API 24-28: Public Downloads'a yazar.
     */
    private static Uri backupToDownloadsLegacy(Context context, File dbFile, String fileName) throws Exception {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir == null) throw new IllegalStateException("Downloads dir null");
        if (!downloadsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            downloadsDir.mkdirs();
        }

        File outFile = new File(downloadsDir, fileName);

        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = new FileOutputStream(outFile, false)) {
            copy(in, out);
        }

        MediaScannerConnection.scanFile(
                context,
                new String[]{ outFile.getAbsolutePath() },
                new String[]{ SQLITE_MIME },
                null
        );

        return Uri.fromFile(outFile);
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
    }

    private static String buildBackupFileName(String dbName, String userName) {
        String base = sanitizeDbBaseName(dbName);
        String ts = TS_FORMAT.format(new Date());

        if (userName != null && !userName.trim().isEmpty()) {
            String u = sanitizeFilePart(userName);
            return base + "_backup_" + u + "_" + ts + ".db";
        }

        return base + "_backup_" + ts + ".db";
    }

    private static String sanitizeDbBaseName(String dbName) {
        if (dbName == null) return "database";
        String x = dbName.trim();
        if (x.isEmpty()) return "database";
        if (x.toLowerCase(Locale.US).endsWith(".db")) {
            return x.substring(0, x.length() - 3);
        }
        return x;
    }

    private static String sanitizeFilePart(String s) {
        if (s == null) return "user";
        String x = s.trim();
        if (x.isEmpty()) return "user";

        // Dosya adında riskli karakterleri temizle
        x = x.replaceAll("[\\\\/:*?\"<>|\\n\\r\\t]", "_");
        x = x.replaceAll("\\s+", "_");
        x = x.replaceAll("[^\\p{L}\\p{N}_-]", "_");

        // Çok uzamasın
        if (x.length() > 40) x = x.substring(0, 40);

        return x;
    }

//    private static String sanitizeFilePart(String s) {
//        if (s == null) return "user";
//        String x = s.trim();
//        if (x.isEmpty()) return "user";
//
//        x = x.replaceAll("[\\/:*?\"<>|\n\r\t]", "_");
//        x = x.replaceAll("\s+", "_");
//        x = x.replaceAll("[^\p{L}\p{N}_-]", "_");
//
//        if (x.length() > 40) x = x.substring(0, 40);
//
//        return x;
//    }
}

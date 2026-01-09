package com.example.rainbootshopnote.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DbBackupExporter {
    private DbBackupExporter() {}

    // Internal DB dosyan:
    public static File getInternalDbFile(Context context) {
        return context.getDatabasePath(DatabaseManager.DB_NAME);
    }

    /** Downloads'a otomatik yazar (API 29+). */
    public static Uri exportToDownloads(Context context) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw new IOException("API<29 için SAF kullanın.");
        }

        File dbFile = getInternalDbFile(context);
        if (!dbFile.exists()) throw new FileNotFoundException("DB not found");

        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "shopnote_backup_" + ts + ".db";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/x-sqlite3");
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri itemUri = resolver.insert(collection, values);
        if (itemUri == null) throw new IOException("Insert failed");

        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = resolver.openOutputStream(itemUri)) {

            if (out == null) throw new IOException("OutputStream null");

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.flush();
        }

        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(itemUri, values, null, null);

        return itemUri;
    }
}

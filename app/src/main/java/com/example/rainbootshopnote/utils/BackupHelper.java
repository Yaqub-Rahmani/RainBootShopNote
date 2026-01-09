package com.example.rainbootshopnote.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

/**
 * Online (Firebase Storage) DB backup helper.
 *
 * - Aynı isimle tekrar gönderilirse Firebase Storage'da aynı path'e upload yapılır (overwrite).
 * - "listele-sil-yükle" yaklaşımı kaldırıldı (daha stabil ve daha az karmaşa).
 */
public final class BackupHelper {

    private BackupHelper() {}

    /**
     * DB dosyasını Firebase Storage'a gönderir.
     *
     * @param context    Context
     * @param dialog     Progress/Custom dialog (null olabilir)
     * @param backupName Dosya adı (uzantısız) — örn: "user_1234" veya "user_1234_20260107_131500"
     */
    public static void backupDatabaseToFirebase(Context context, AlertDialog dialog, String backupName) {
        if (backupName == null) backupName = "backup";
        backupName = backupName.trim();
        if (backupName.isEmpty()) backupName = "backup";

        // DB file path
        File dbFile = context.getDatabasePath(DBHelper.DB_name);

        if (!dbFile.exists()) {
            Toast.makeText(context, "دیتابیس پیدا نشد.", Toast.LENGTH_SHORT).show();
            safeDismiss(dialog);
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference rootRef = storage.getReference();

        // NOTE: Bu path sabittir; aynı isimle upload olursa overwrite olur.
        StorageReference dbRef = rootRef.child("MyBackUpFiles").child(backupName + ".db");

        uploadDatabaseToFirebase(context, dbFile, dbRef, dialog);
    }

    private static void uploadDatabaseToFirebase(Context context, File dbFile, StorageReference dbRef, AlertDialog dialog) {
        Uri fileUri = Uri.fromFile(dbFile);
        UploadTask uploadTask = dbRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(context, "دیتابیس ارسال شد.", Toast.LENGTH_SHORT).show();
            safeDismiss(dialog);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "دیتابیس ارسال پشتیبان: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // دیالوگ را باز می‌گذاریم تا کاربر خودش تصمیم بگیرد.
        });
    }

    private static void safeDismiss(AlertDialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
        } catch (Exception ignore) {}
    }
}



//package com.example.rainbootshopnote.utils;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.net.Uri;
//import android.widget.Toast;
//
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//
//import java.io.File;
//
//public class BackupHelper {
//
//    public static void backupDatabaseToFirebase(Context context, AlertDialog dialog, String backupName) {
//        File dbFile = new File(context.getDatabasePath("rainbootnote.db").getPath());
//
//        if (!dbFile.exists()) {
//            Toast.makeText(context, "دیتابیس موجود نیست!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference();
//
//        // backupName'dan kullanıcı adı ve şifreyi ayır
//        String userInfoName = backupName.split("\\$")[0]; // 3636363636_3366
//
//        // Mevcut dosyaları kontrol et
//        storageRef.listAll().addOnSuccessListener(listResult -> {
//            boolean fileExists = false;
//
//            for (StorageReference item : listResult.getItems()) {
//                String itemName = item.getName();
//                String itemUserInfoName = itemName.split("\\$")[0]; // 3636363636_3366
//
//                // Sadece kullanıcı adı ve şifre kısmını kontrol et
//                if (itemUserInfoName.equals(userInfoName)) {
//                    fileExists = true;
//                    // Eski dosyayı sil
//                    item.delete().addOnSuccessListener(aVoid -> {
//                        // Silme başarılıysa yeni dosyayı yükle
//                        uploadDatabaseToFirebase(dbFile, storageRef.child("MyBackUpFiles/" + backupName + ".db"), dialog);
//                    }).addOnFailureListener(e -> {
//                        // Eğer silme başarısız olursa yine yeni dosyayı yükle
//                        uploadDatabaseToFirebase(dbFile, storageRef.child("MyBackUpFiles/" + backupName + ".db"), dialog);
//                    });
//                    break; // Eşleşme bulundu, döngüyü sonlandır
//                }
//            }
//
//            // Eğer dosya yoksa doğrudan yükleme yap
//            if (!fileExists) {
//                uploadDatabaseToFirebase(dbFile, storageRef.child("MyBackUpFiles/" + backupName + ".db"), dialog);
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(context, "خطا در زمان پشتیبان گیری: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    // Yedekleme işlemini gerçekleştiren metod
//    private static void uploadDatabaseToFirebase(File dbFile, StorageReference dbRef, AlertDialog dialog) {
//        Uri fileUri = Uri.fromFile(dbFile);
//        UploadTask uploadTask = dbRef.putFile(fileUri);
//
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            Toast.makeText(dialog.getContext(), "دیتابیس ارسال شد!", Toast.LENGTH_SHORT).show();
//            dialog.dismiss();
//        }).addOnFailureListener(e -> {
//            Toast.makeText(dialog.getContext(), "خطا در زمان پشتیبان گیری: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//}

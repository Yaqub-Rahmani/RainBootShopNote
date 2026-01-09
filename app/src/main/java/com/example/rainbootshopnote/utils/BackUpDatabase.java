package com.example.rainbootshopnote.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BackUpDatabase {

    private String verificationId;  // Global değişken

    public void sendVerificationCode(String phoneNumber, Activity activity) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(phoneNumber)       // Doğrulanacak telefon numarası
                        .setTimeout(60L, TimeUnit.SECONDS) // Zaman aşımı
                        .setActivity(activity)             // Aktivite referansı
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(PhoneAuthCredential credential) {
                                // Firebase, kodu otomatik olarak doğruladığında
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(FirebaseException e) {
                                // Doğrulama işlemi başarısız olduğunda
                                Log.e("Verification Failed", e.getMessage());
                                Toast.makeText(activity, "Doğrulama başarısız: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                // Doğrulama kodu gönderildiğinde, verificationId'yi kaydediyoruz
                                BackUpDatabase.this.verificationId = verificationId;
                                Log.d("Code Sent", "Doğrulama kodu gönderildi: " + verificationId);
                                Toast.makeText(activity, "Kod gönderildi, lütfen doğrulama kodunu girin.", Toast.LENGTH_SHORT).show();
                            }
                        }).build();

        // Doğrulama isteğini başlatıyoruz
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyCode(String code) {
        // Kullanıcı doğrulama kodunu girdiğinde bu fonksiyon çağrılır
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Başarılı doğrulama
                            Log.d("SignIn", "Telefon numarası ile giriş başarılı.");

                        } else {
                            // Doğrulama başarısız
                            Log.e("SignIn", "Telefon numarası ile giriş başarısız.");
                        }
                    }
                });
    }

    public String getDeviceIdentifier(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String serialNumber = Build.SERIAL;

        // Eğer Android ID mevcutsa onu kullan, yoksa seri numarasına geç
        if (!androidId.isEmpty()) {
            return androidId;
        } else {
            return serialNumber;
        }
    }

    public String createBackupFileName(String phoneNumber, String deviceId) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return phoneNumber + "_" + deviceId + "_" + timestamp + ".db";
    }

//    public void backupDatabaseToFirebase(Context context, String fileName) {
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference().child("SqliteShoteFiles/" + fileName);
//
//        // Veritabanı dosyasının yolu
//        String dbPath = "data/data/" + context.getPackageName() + "/databases/your_database_name.db";
//        Uri file = Uri.fromFile(new File(dbPath));
//
//        UploadTask uploadTask = storageRef.putFile(file);
//
//        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // Yükleme başarılı
//                Log.d("Backup", "Backup successfully uploaded");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Yükleme başarısız
//                Log.e("Backup", "Backup failed: " + exception.getMessage());
//            }
//        });
//    }
//
//    public void deletePreviousBackup(String previousFileName) {
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference oldBackupRef = storage.getReference().child("SqliteShoteFiles/" + previousFileName);
//
//        oldBackupRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                // Eski yedek başarıyla silindi
//                Log.d("Backup", "Previous backup deleted");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Silme başarısız
//                Log.e("Backup", "Failed to delete previous backup: " + exception.getMessage());
//            }
//        });
//    }
}

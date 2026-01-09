package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.admins;
import com.example.rainbootshopnote.utils.BackupHelper;

public class InfoActivity extends AppCompatActivity {

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);

        Button backUpButton = findViewById(R.id.btn_addBackUp);

        backUpButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this)) {
                Toast.makeText(InfoActivity.this, "لطفاً به اینترنت وصل شوید.", Toast.LENGTH_SHORT).show();
                return;
            }

            backUpButton.setVisibility(View.GONE);

            LayoutInflater dialogInflater = LayoutInflater.from(this);
            View backUpView = dialogInflater.inflate(R.layout.item_backup, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(backUpView).setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.setOnDismissListener(d -> backUpButton.setVisibility(View.VISIBLE));

            Button submitBackUp = backUpView.findViewById(R.id.MobileSubmit);
            Button cancelButton = backUpView.findViewById(R.id.CancelProductSubmit);

            LinearLayout codeLayout = backUpView.findViewById(R.id.outItemHideCode);
            LinearLayout mobileLayout = backUpView.findViewById(R.id.outItemHideMobile);

            submitBackUp.setText(getResources().getString(R.string.sendDatabase));
            codeLayout.setVisibility(View.GONE);
            mobileLayout.setVisibility(View.GONE);

            submitBackUp.setOnClickListener(v1 -> {
                db = new DBHelper(this);
                admins firstAdmin = db.getFirstAdmin();

                if (firstAdmin == null) {
                    Toast.makeText(InfoActivity.this, "لطفاً ابتدا خود را در سیستم راجستر کنید.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                // NOTE: امنیت: بهتر است رمز را داخل نام فایل نگذارید.
                // فعلاً برای حفظ رفتار فعلی شما همین ساختار نگه داشته شده است.
                String userInfoName = firstAdmin.getUser_name() + "_" + firstAdmin.getUser_password();
                userInfoName = userInfoName == null ? "" : userInfoName.trim();

                if (userInfoName.isEmpty()) {
                    Toast.makeText(InfoActivity.this, "نام کاربر یا رمز خالی است.", Toast.LENGTH_SHORT).show();
                    return;
                }

                BackupHelper.backupDatabaseToFirebase(InfoActivity.this, dialog, userInfoName);
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
        });
    }

    private boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.admins;
import com.example.rainbootshopnote.security.appSecur.ActivationGate;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    private static final String SUPPORT_PHONE = "0789505257";

    private Button btnSubmit;
    private DBHelper db;
    private Context myContext = this;
    private admins loginAdmin;

    private TextView txtShowPin, txtHideClick;
    private EditText etxtPin;

    private AlertDialog activationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // --- DB ---
        db = new DBHelper(myContext);
        // Not: Bunu her açılışta çağırmak doğru değilse kaldır. Mevcut koddaki gibi bıraktım.
        db.onUpgrade(db.getWritableDatabase(), 1, 2);

        // --- UI bind ---
        etxtPin = findViewById(R.id.etxtPin);
        txtShowPin = findViewById(R.id.txtShowNote);
        btnSubmit = findViewById(R.id.btnLoginSubmit);
        txtHideClick = findViewById(R.id.txtHideClick);

        txtHideClick.setVisibility(View.VISIBLE);
        txtHideClick.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, InfoActivity.class)));

        // --- Get admin ---
        loginAdmin = db.getLastOrDefaultAdmin();
        int note = Integer.parseInt(loginAdmin.getUser_password());

        // if status true => show pin
        if (loginAdmin.getStatus()) txtShowPin.setText(String.valueOf(note));

        // --- PIN submit ---
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Eğer aktivasyon yoksa PIN işlemine izin verme
                if (!ActivationGate.isActivatedForThisDevice(LoginActivity.this)) {
                    runActivationFlowIfNeeded(); // dialog açar
                    return;
                }

                // Expiry kontrol (istersen aktif et)
                if (ActivationGate.isExpiredNow()) {
                    showExpiredDialogAndClose();
                    return;
                }

                // PIN kontrol
                String pinText = etxtPin.getText().toString();
                try {
                    int userPin = Integer.parseInt(pinText);

                    if (userPin == note) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(LoginActivity.this, "رمز اشتباه است.", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(LoginActivity.this, "رمز خالی است", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        runActivationFlowIfNeeded();
    }

    // ---------------------------------------------------------------------
    // Activation + Expiry Flow
    // ---------------------------------------------------------------------

    private void runActivationFlowIfNeeded() {
        // Expiry (offline) — istersen burada zorunlu tut
        if (ActivationGate.isExpiredNow()) {
            showExpiredDialogAndClose();
            return;
        }

        // Aktivasyon zaten varsa hiçbir şey yapma
        if (ActivationGate.isActivatedForThisDevice(this)) {
            if (activationDialog != null && activationDialog.isShowing()) activationDialog.dismiss();
            return;
        }

        // Dialog zaten açıksa tekrar açma
        if (activationDialog != null && activationDialog.isShowing()) return;

        showActivationDialog();
    }

    private void showExpiredDialogAndClose() {
        new AlertDialog.Builder(this)
                .setTitle("پایان اعتبار")
                .setMessage("اعتبار این نسخه به پایان رسیده است.\nلطفاً به دفتر مراجعه کنید.")
                .setCancelable(false)
                .setPositiveButton("بستن", (d, w) -> finishAffinity())
                .show();
    }

    private void showActivationDialog() {
        // Root layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        int pad = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 18, getResources().getDisplayMetrics()
        );
        root.setPadding(pad, pad, pad, pad);

        // Title (inside)
        TextView title = new TextView(this);
        title.setText("فعال‌سازی لازم است");
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setPadding(0, 0, 0, pad / 2);
        root.addView(title);

        // Info
        TextView info = new TextView(this);
        info.setText("برای استفاده از برنامه، کُد فعال‌سازی را وارد کنید.");
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        info.setPadding(0, 0, 0, pad);
        root.addView(info);

        // Phone line - clickable (bigger)

        SpannableString sp = new SpannableString("☎ دریافت کُد: " + SUPPORT_PHONE);
        TextView phone = new TextView(this);
        phone.setText(sp);
//        phone.setText("☎ " + SUPPORT_PHONE);
        phone.setTypeface(Typeface.DEFAULT_BOLD);
        phone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        phone.setPadding(0, 0, 0, pad);
        phone.setClickable(true);
        phone.setOnClickListener(v -> openDialer(SUPPORT_PHONE));
        root.addView(phone);


        // Input
        final EditText input = new EditText(this);
        input.setHint("کُد فعال‌سازی");

//        input.setGravity(Gravity.CENTER_HORIZONTAL);

        input.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{ new DigitsAnyLocaleFilter(11) });
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        root.addView(input);

        // Dialog
        activationDialog = new AlertDialog.Builder(this)
                .setView(root)
                .setCancelable(false)
                .setNegativeButton("بستن", (d, which) -> finishAffinity())
                .setPositiveButton("تأیید", null) // override below
                .create();

        activationDialog.setOnShowListener(d -> {
            final android.widget.Button okBtn =
                    activationDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            okBtn.setEnabled(false);

            // 11 hane olmadan enable olmasın
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String latin = ActivationGate.toLatinDigits(s == null ? "" : s.toString()).trim();
                    okBtn.setEnabled(latin.length() == 11);
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            okBtn.setOnClickListener(v -> {
                String raw = input.getText() == null ? "" : input.getText().toString();
                String latin = ActivationGate.toLatinDigits(raw).trim();

                if (latin.length() != 11) {
                    input.setError("کُد باید دقیقاً ۱۱ رقم باشد.");
                    return;
                }

                boolean valid = ActivationGate.validateKeyNow(LoginActivity.this, latin);
                if (valid) {
                    ActivationGate.markActivated(LoginActivity.this);
                    Toast.makeText(LoginActivity.this, "فعال‌سازی انجام شد.", Toast.LENGTH_SHORT).show();
                    activationDialog.dismiss();
                } else {
                    input.setError("کُد اشتباه است.");
                }
            });
        });

        activationDialog.show();
    }

    private void openDialer(String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {}
    }

    /**
     * 11 hane + Farsça/Arabic-Indic/Latin rakam izinli filtre
     * - Fazla girişe izin vermez
     * - Rakamsız karakterleri engeller
     */
    static class DigitsAnyLocaleFilter implements InputFilter {
        private final int maxLen;

        DigitsAnyLocaleFilter(int maxLen) {
            this.maxLen = maxLen;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            int keep = maxLen - (dest.length() - (dend - dstart));
            if (keep <= 0) return "";
            if ((end - start) > keep) end = start + keep;

            StringBuilder out = new StringBuilder();
            for (int i = start; i < end; i++) {
                char ch = source.charAt(i);

                boolean latin = (ch >= '0' && ch <= '9');
                boolean persian = (ch >= 0x06F0 && ch <= 0x06F9);
                boolean arabicIndic = (ch >= 0x0660 && ch <= 0x0669);

                if (latin || persian || arabicIndic) out.append(ch);
            }
            return out.toString();
        }
    }
}

package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.admins;
import com.example.rainbootshopnote.entities.products;
import com.example.rainbootshopnote.entities.users;
import com.example.rainbootshopnote.utils.BackupHelper;
import com.example.rainbootshopnote.utils.DbImportExportManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SheetDialogFragment extends BottomSheetDialogFragment {

    private DBHelper db;

    // Import picker
    private ActivityResultLauncher<String[]> pickDbLauncher;
    private Uri pendingDbUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickDbLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;

                    // Sadece .db kabul
                    if (!isDbFile(uri)) {
                        Toast.makeText(requireContext(),
                                "فقط فایل دیتابیس (.db) را انتخاب کنید.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // PIN قبلاً گرفته شده؛ مستقیم import
                    importDatabaseWithBackup(uri);
                }
        );
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.activity_sheet_dialog_fragment, container, false);

        Button btnHome = view.findViewById(R.id.btn_Home);
        Button btnAddProduct = view.findViewById(R.id.btn_addProductDialog);
        Button btnAddUser = view.findViewById(R.id.btn_addUser);
        Button btnChangePassword = view.findViewById(R.id.btn_addChangePassword);
        Button btnDbTools = view.findViewById(R.id.btn_addBackUp);

        db = new DBHelper(requireContext());

        btnHome.setOnClickListener(v -> {
            dismiss();
            if (getActivity() != null) {
                ((MainActivity) getActivity()).loadFragment(new DashboardFragment());
            }
        });

        btnAddProduct.setOnClickListener(v -> openAddProductDialog());
        btnAddUser.setOnClickListener(v -> openAddUserDialog());
        btnChangePassword.setOnClickListener(v -> openChangePasswordDialog());

        btnDbTools.setOnClickListener(v -> openDbActionsSheet());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try { if (db != null) db.close(); } catch (Exception ignore) {}
    }

    // ---------------------------
    // DB Actions Sheet
    // ---------------------------
    private void openDbActionsSheet() {
        DatabaseActionsSheetDialogFragment sheet = new DatabaseActionsSheetDialogFragment();
        sheet.setListener(new DatabaseActionsSheetDialogFragment.Listener() {

            @Override
            public void onExportDownloads() {
                // Export için PIN
                exportDownloads();
            }

            @Override
            public void onImportDb() {
                showPinDialog("بارگذاری دیتابیس", ok -> {
                    if (!ok) return;

                    try {
                        pickDbLauncher.launch(new String[]{
                                "application/x-sqlite3",
                                "application/vnd.sqlite3",
                                "application/octet-stream"
                        });
                    } catch (Exception ignore) {
                        pickDbLauncher.launch(new String[]{"*/*"});
                    }
                });
            }



            @Override
            public void onSendOnline() {
                // Online için PIN
                showPinDialog("ارسال کاپی دیتابیس به آنلاین", ok -> {
                    if (!ok) return;
                    runFirebaseBackupFlow();
                });
            }

            @Override
            public void onReportsPdf() {
                Toast.makeText(requireContext(), "گزارشات / PDF", Toast.LENGTH_SHORT).show();
            }
        });

        sheet.show(getParentFragmentManager(), "db_actions");
    }

    // ---------------------------
    // PIN dialog (4 digits)
    // ---------------------------
    private interface PinCallback { void onResult(boolean ok); }

    private void showPinDialog(String titleFa, PinCallback cb) {
        DBHelper tmp = new DBHelper(requireContext());
        admins firstAdmin = tmp.getLastOrDefaultAdmin();

        if (firstAdmin == null || firstAdmin.getUser_password() == null) {
            Toast.makeText(requireContext(), "ابتدا خود را به سیستم راجستر کنید.", Toast.LENGTH_SHORT).show();
            try { tmp.close(); } catch (Exception ignore) {}
            cb.onResult(false);
            return;
        }

        final String expectedPin = firstAdmin.getUser_password().trim();
        try { tmp.close(); } catch (Exception ignore) {}

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_password_pin, null);

        TextView txtTitle = dialogView.findViewById(R.id.txtPinTitle);
        if (txtTitle != null) txtTitle.setText(titleFa);

        TextInputLayout til = dialogView.findViewById(R.id.tilPin);
        TextInputEditText et = dialogView.findViewById(R.id.etPin);

        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        Button okBtn = dialogView.findViewById(R.id.SaveProductSubmit);
        Button cancelBtn = dialogView.findViewById(R.id.CancelProductSubmit);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        cancelBtn.setOnClickListener(v -> {
            dlg.dismiss();
            cb.onResult(false);
        });

        okBtn.setOnClickListener(v -> {
            String entered = (et.getText() == null) ? "" : et.getText().toString().trim();

            if (entered.length() != 4) {
                til.setError("رمز باید ۴ عدد باشد.");
                return;
            }
            if (!entered.equals(expectedPin)) {
                til.setError("رمز اشتباه است.");
                return;
            }

            til.setError(null);
            dlg.dismiss();
            cb.onResult(true);
        });

        dlg.show();
    }

    // ---------------------------
    // Existing dialogs
    // ---------------------------
    private void openAddProductDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        Button addButton = dialogView.findViewById(R.id.SaveProductSubmit);
        Button cancelButton = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView title = dialogView.findViewById(R.id.txtTitle);
        title.setText(getResources().getString(R.string.dyalog_product));

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogView.findViewById(R.id.outItem4);

        out1.setHint(getResources().getString(R.string.productName));
        out2.setHint(getResources().getString(R.string.productQuantity));
        out3.setHint(getResources().getString(R.string.productPrice));
        out4.setHint(getResources().getString(R.string.productDescription));

        TextInputEditText t1 = dialogView.findViewById(R.id.txtItem1);
        TextInputEditText t2 = dialogView.findViewById(R.id.txtItem2);
        TextInputEditText t3 = dialogView.findViewById(R.id.txtItem3);
        TextInputEditText t4 = dialogView.findViewById(R.id.txtItem4);

        addButton.setOnClickListener(v -> {
            String name = t1.getText() == null ? "" : t1.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "اسم خالی است!", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity;
            float price;

            try {
                quantity = Integer.parseInt(t2.getText().toString().trim());
            } catch (Exception e) {
                Toast.makeText(requireContext(), "تعداد خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                BigDecimal priceBD = new BigDecimal(t3.getText().toString().trim());
                priceBD = priceBD.setScale(2, RoundingMode.HALF_UP);
                price = priceBD.floatValue();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "قیمت خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            String desc = t4.getText() == null ? "" : t4.getText().toString().trim();

            DBHelper dbLocal = new DBHelper(requireContext());
            products p = new products(0, "999", name, price, quantity, true, desc, false);
            boolean success = dbLocal.addProduct(p);

            Toast.makeText(requireContext(),
                    success ? "جنس ثبت گردید !" : "این جنس موجود است!",
                    Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCurrentFragment();
            }

            dialog.dismiss();
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            dismiss();
        });
    }

    private void openAddUserDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        Button addButton = dialogView.findViewById(R.id.SaveProductSubmit);
        Button cancelButton = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView title = dialogView.findViewById(R.id.txtTitle);
        title.setText(getResources().getString(R.string.dyalog_user));

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);

        out1.setHint(getResources().getString(R.string.userName));
        out2.setHint(getResources().getString(R.string.userMobile));
        out3.setHint(getResources().getString(R.string.userAdders));

        TextInputEditText t1 = dialogView.findViewById(R.id.txtItem1);
        TextInputEditText t2 = dialogView.findViewById(R.id.txtItem2);
        TextInputEditText t3 = dialogView.findViewById(R.id.txtItem3);
        TextInputEditText t4 = dialogView.findViewById(R.id.txtItem4);

        t2.setInputType(InputType.TYPE_CLASS_PHONE);
        t3.setInputType(InputType.TYPE_CLASS_TEXT);

        addButton.setOnClickListener(v -> {
            String name = t1.getText() == null ? "" : t1.getText().toString().trim();
            String mobile = t2.getText() == null ? "" : t2.getText().toString().trim();
            String address = t3.getText() == null ? "" : t3.getText().toString().trim();
            String description = t4.getText() == null ? "" : t4.getText().toString().trim();

            if (name.isEmpty()) { Toast.makeText(requireContext(), "اسم خالی است!", Toast.LENGTH_SHORT).show(); return; }
            if (mobile.isEmpty()) { Toast.makeText(requireContext(), "موبایل خالی است!", Toast.LENGTH_SHORT).show(); return; }
            if (!mobile.matches("^\\d{10}$")) { Toast.makeText(requireContext(), "موبایل نامعتبر است!", Toast.LENGTH_SHORT).show(); return; }
            if (address.isEmpty()) { Toast.makeText(requireContext(), "آدرس خالی است!", Toast.LENGTH_SHORT).show(); return; }

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String now = fmt.format(new Date());

            DBHelper dbLocal = new DBHelper(requireContext());
            users u = new users(0, name, mobile, address, now, now, true, description, false);
            boolean success = dbLocal.addUser(u);

            Toast.makeText(requireContext(),
                    success ? "شخص ثبت گردید !" : "این شخص موجود است!",
                    Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCurrentFragment();
            }

            dialog.dismiss();
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            dismiss();
        });
    }

    private void openChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        Button addButton = dialogView.findViewById(R.id.SaveProductSubmit);
        Button cancelButton = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView title = dialogView.findViewById(R.id.txtTitle);
        title.setText(getResources().getString(R.string.dyalog_password));

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);

        out1.setHint(getResources().getString(R.string.userFirstName));
        out2.setHint(getResources().getString(R.string.userMobile));
        out3.setHint(getResources().getString(R.string.userPassword));

        TextInputEditText t1 = dialogView.findViewById(R.id.txtItem1);
        TextInputEditText t2 = dialogView.findViewById(R.id.txtItem2);
        TextInputEditText t3 = dialogView.findViewById(R.id.txtItem3);

        t2.setInputType(InputType.TYPE_CLASS_PHONE);
        t3.setInputType(InputType.TYPE_CLASS_NUMBER);
        t3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        addButton.setOnClickListener(v -> {
            String name = t1.getText() == null ? "" : t1.getText().toString().trim();
            String mobile = t2.getText() == null ? "" : t2.getText().toString().trim();
            String password = t3.getText() == null ? "" : t3.getText().toString().trim();

            if (name.isEmpty()) { Toast.makeText(requireContext(), "اسم خالی است!", Toast.LENGTH_SHORT).show(); return; }
            if (mobile.isEmpty()) { Toast.makeText(requireContext(), "مبایل خالی است!", Toast.LENGTH_SHORT).show(); return; }
            if (!mobile.matches("^\\d{10}$")) { Toast.makeText(requireContext(), "موبایل نامعتبر است!", Toast.LENGTH_SHORT).show(); return; }
            if (password.length() != 4) { Toast.makeText(requireContext(), "رمز باید ۴ عدد باشد!", Toast.LENGTH_SHORT).show(); return; }

            DBHelper dbLocal = new DBHelper(requireContext());
            boolean success = dbLocal.addAdmin(new admins(name, mobile, password, true, false));

            Toast.makeText(requireContext(),
                    success ? "رمز تبدیل گردید !" : "رمز تبدیل نشد!",
                    Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).updateCurrentFragment();
            }

            dialog.dismiss();
            dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            dismiss();
        });
    }

    // ---------------------------
    // 1) Export Downloads (PIN -> doExport)
    // ---------------------------
    private void exportDownloads() {
        showPinDialog("ذخیره پشتیبان", ok -> {
            if (!ok) return;
            doExportDownloads();
        });


    }

    private void doExportDownloads() {
        try {
            DBHelper dbLocal = new DBHelper(requireContext());
            admins firstAdmin = dbLocal.getLastOrDefaultAdmin();

            String userInfoName = (firstAdmin != null)
                    ? (firstAdmin.getUser_name() + "_" + firstAdmin.getUser_password())
                    : "user";

            dbLocal.close();

            DbImportExportManager.backupToDownloads(requireContext(), DBHelper.DB_name, userInfoName);
            Toast.makeText(requireContext(), "پشتیبان در پوشه دانلود ذخیره شد.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "ذخیره پشتیبان ناکام شد.", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------
    // 2) Import DB (PIN zaten dosya seçince soruluyor)
    // ---------------------------
    private void importDatabaseWithBackup(Uri uri) {
        if (uri == null) return;

        try {
            // Import öncesi backup (PIN tekrar sormasın)
            doExportDownloads();

            // DB bağlantısını kapat
            try { if (db != null) db.close(); } catch (Exception ignore) {}

            DbImportExportManager.importDatabaseFromUri(requireContext(), uri, DBHelper.DB_name);

            // DB yeniden aç
            db = new DBHelper(requireContext());

            Toast.makeText(requireContext(), "دیتابیس با موفقیت بارگذاری شد.", Toast.LENGTH_SHORT).show();
            dismiss();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "بارگذاری دیتابیس ناکام شد.", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------
    // 3) Online backup (PIN openDbActionsSheet içinde)
    // ---------------------------
    private void runFirebaseBackupFlow() {
        try {
            DBHelper dbLocal = new DBHelper(requireContext());
            admins firstAdmin = dbLocal.getLastOrDefaultAdmin();

            if (firstAdmin == null) {
                Toast.makeText(requireContext(), "لطفاً ابتدا خود را راجستر کنید.", Toast.LENGTH_SHORT).show();
                dbLocal.close();
                return;
            }

            String userInfoName = firstAdmin.getUser_name() + "_" + firstAdmin.getUser_password();
            dbLocal.close();

            AlertDialog dlg = new AlertDialog.Builder(requireContext())
                    .setTitle("ارسال کاپی دیتابیس")
                    .setMessage("لطفاً منتظر بمانید...")
                    .setCancelable(false)
                    .create();
            dlg.show();

            BackupHelper.backupDatabaseToFirebase(requireContext(), dlg, userInfoName);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "ارسال کاپی ناکام شد.", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    private boolean isDbFile(Uri uri) {
        try {
            String name = null;

            try (android.database.Cursor c = requireContext().getContentResolver()
                    .query(uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) name = c.getString(idx);
                }
            }

            if (name == null) return false;
            name = name.toLowerCase(Locale.ROOT).trim();
            return name.endsWith(".db");

        } catch (Exception ignore) {
            return false;
        }
    }
}

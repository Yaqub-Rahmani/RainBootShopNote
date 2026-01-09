package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rainbootshopnote.Dto.UserDTO;
import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.deposits;
import com.example.rainbootshopnote.entities.users;
import com.example.rainbootshopnote.entities.withdrawals;
import com.example.rainbootshopnote.myAdapters.UserDTOListAdapter;
import com.example.rainbootshopnote.utils.date.AppDate;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserFragment extends Fragment implements MainActivity.FragmentUpdater {

    private DBHelper db;

    // UI
    private ListView listViewUsers;
    private TextView shortInfoTitle;
    private UserDTOListAdapter adapter;

    // Data
    private final List<UserDTO> allList = new ArrayList<>();
    private final List<UserDTO> filteredList = new ArrayList<>();

    // State
    private String searchQuery = "";
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);

        db = new DBHelper(requireContext());

        // Header include (CashCoins ile aynı ID)
        View header = view.findViewById(R.id.includeHeader);
        TextView txtHeaderTitle = header.findViewById(R.id.txtHeaderTitle);

        View searchContainer = header.findViewById(R.id.search_container);
        TextInputEditText etHeaderSearch = header.findViewById(R.id.etHeaderSearch);

        View btnHeaderSearch = header.findViewById(R.id.btnHeaderSearch);
        View btnHeaderFilter = header.findViewById(R.id.btnHeaderFilter);
        View btnHeaderPdf = header.findViewById(R.id.btnHeaderPdf);

        // Bu ekranda filter yok
        btnHeaderFilter.setVisibility(View.GONE);

        txtHeaderTitle.setText(getString(R.string.infoUserFragmentTitle));

        // UI
        shortInfoTitle = view.findViewById(R.id.txtItemShortInfo);
        listViewUsers = view.findViewById(R.id.user_info_listView);

        // Adapter
        adapter = new UserDTOListAdapter(requireContext(), filteredList);
        listViewUsers.setAdapter(adapter);

        // Load
        refreshAndRender();

        // Search toggle
        btnHeaderSearch.setOnClickListener(v -> {
            if (searchContainer.getVisibility() == View.VISIBLE) {
                searchContainer.startAnimation(
                        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                );
                searchContainer.setVisibility(View.GONE);
            } else {
                searchContainer.setVisibility(View.VISIBLE);
                searchContainer.startAnimation(
                        AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
                );
                etHeaderSearch.requestFocus();
            }
        });

        // Restore search text
        etHeaderSearch.setText(searchQuery);

        // Search watcher
        etHeaderSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = (s == null) ? "" : s.toString().trim();
                applySearchOnly();
            }
        });

        // PDF
        btnHeaderPdf.setOnClickListener(v -> {
            List<UserDTO> data = new ArrayList<>(filteredList);

            String title = getString(R.string.infoUserFragmentTitle);
            String html = buildUsersPdfHtml(title, data, searchQuery);

            com.example.rainbootshopnote.utils.PdfExportHelper.printHtml(
                    requireContext(),
                    title,
                    html
            );
        });

        // Item click -> actions
        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                if (position < 0 || position >= filteredList.size()) return;
                UserDTO u = filteredList.get(position);
                if (u == null) return;
                showUserActions(u);
            }
        });

        return view;
    }

    private void refreshAndRender() {
        List<UserDTO> dbList = safeList(db.getUsersWithAmounts());
        allList.clear();
        for (UserDTO u : dbList) {
            if (u == null) continue;
            allList.add(u);
        }
        applySearchOnly();
    }

    private void applySearchOnly() {
        filteredList.clear();

        String q = (searchQuery == null) ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filteredList.addAll(allList);
        } else {
            for (UserDTO u : allList) {
                if (u == null) continue;
                String name = nullToEmpty(u.getName()).toLowerCase(Locale.ROOT);
                String mobile = nullToEmpty(u.getMobile()).toLowerCase(Locale.ROOT);
                if (name.contains(q) || mobile.contains(q)) {
                    filteredList.add(u);
                }
            }
        }

        if (adapter != null) adapter.updateList(filteredList);
        updateShortInfo();
    }

    private void updateShortInfo() {
        double totalExcessWithdrawals = 0.0; // جمله باقی
        double totalExcessDeposits = 0.0;    // جمله رسید

        for (UserDTO user : filteredList) {
            if (user == null) continue;

            double withdrawal = user.getWithdrawalsAmount();
            double deposit = user.getDepositAmount();
            double net = deposit - withdrawal;

            if (net > 0) totalExcessDeposits += net;
            else totalExcessWithdrawals += Math.abs(net);
        }

        shortInfoTitle.setText(
                "جمله رسید: " + df.format(totalExcessDeposits) +
                        " | جمله باقی: " + df.format(totalExcessWithdrawals)
        );
    }

    private void showUserActions(UserDTO userDTO) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.item_product_more_option, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        Button btnEdit = dialogView.findViewById(R.id.btn_item_product_edit);
        Button btnDeposit = dialogView.findViewById(R.id.btn_item_product_add_count);
        Button btnWithdrawal = dialogView.findViewById(R.id.btn_item_product_sale);
        Button btnDelete = dialogView.findViewById(R.id.btn_item_product_delete);

        TextView txtName = dialogView.findViewById(R.id.txt_item_product_name);

        btnDeposit.setText(R.string.depositSave);
        btnWithdrawal.setText(R.string.withdrawalSave);

        String fullName = nullToEmpty(userDTO.getName());
        String shortName = safeShort(fullName, 30);

        String sideTitle =
                df.format(userDTO.getWithdrawalsAmount()) + " - باقی." +
                        "  __________  " +
                        df.format(userDTO.getDepositAmount()) + " - رسید.";

        txtName.setText(shortName);

        btnDeposit.setOnClickListener(v -> {
            dialog.dismiss();
            showDepositDialog(userDTO, shortName, sideTitle);
        });

        btnWithdrawal.setOnClickListener(v -> {
            dialog.dismiss();
            showWithdrawalDialog(userDTO, shortName, sideTitle);
        });

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showUserEditDialog(userDTO, shortName);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showUserDeleteDialog(userDTO, shortName, sideTitle);
        });
    }

    // ---------- Dialogs ----------

    private void showDepositDialog(UserDTO userDTO, String shortName, String sideTitle) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dlg.show();

        Button btnSave = dialogView.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.depositSave));
        btnSave.setText(getString(R.string.save));

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogView.findViewById(R.id.outItem4);

        out2.setVisibility(View.GONE);

        out1.setHint(getString(R.string.userName));
        out3.setHint(getString(R.string.amount));
        out4.setHint(getString(R.string.productDescription));

        TextInputEditText t1 = dialogView.findViewById(R.id.txtItem1);
        TextInputEditText t3 = dialogView.findViewById(R.id.txtItem3);
        TextInputEditText t4 = dialogView.findViewById(R.id.txtItem4);

        LinearLayout hide = dialogView.findViewById(R.id.outItemHide1);
        hide.setVisibility(View.VISIBLE);
        ((TextView) dialogView.findViewById(R.id.txtItemHide1)).setText(shortName);
        ((TextView) dialogView.findViewById(R.id.txtItemHide2)).setText(sideTitle);

        btnSave.setOnClickListener(v -> {
            String name = safeText(t1);
            BigDecimal amount = safeMoneyBD(t3);
            String desc = safeText(t4);

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(requireContext(), "مبلغ رسید خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            String now = safeNowDbDateTime();

            deposits d = new deposits(
                    0,
                    name,
                    amount.floatValue(),
                    now,
                    now,
                    true,
                    desc,
                    userDTO.getUser_id(),
                    false
            );

            boolean ok = db.addDeposit(d);
            if (ok) {
                Toast.makeText(requireContext(), "ثبت گردید !", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dlg.dismiss();
            } else {
                Toast.makeText(requireContext(), "موجود است !", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dlg.dismiss());
    }

    private void showWithdrawalDialog(UserDTO userDTO, String shortName, String sideTitle) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dlg.show();

        Button btnSave = dialogView.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.withdrawalSave));
        btnSave.setText(getString(R.string.save));

        LinearLayout hide = dialogView.findViewById(R.id.outItemHide1);
        hide.setVisibility(View.VISIBLE);

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogView.findViewById(R.id.outItem4);

        out2.setVisibility(View.GONE);

        out1.setHint(getString(R.string.userName));
        out3.setHint(getString(R.string.amount));
        out4.setHint(getString(R.string.productDescription));

        TextInputEditText t1 = dialogView.findViewById(R.id.txtItem1);
        TextInputEditText t3 = dialogView.findViewById(R.id.txtItem3);
        TextInputEditText t4 = dialogView.findViewById(R.id.txtItem4);

        ((TextView) dialogView.findViewById(R.id.txtItemHide1)).setText(shortName);
        ((TextView) dialogView.findViewById(R.id.txtItemHide2)).setText(sideTitle);

        btnSave.setOnClickListener(v -> {
            String name = safeText(t1);
            BigDecimal amount = safeMoneyBD(t3);
            String desc = safeText(t4);

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                Toast.makeText(requireContext(), "مبلغ خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            String now = safeNowDbDateTime();

            withdrawals w = new withdrawals(
                    0,
                    name,
                    amount.floatValue(),
                    now,
                    now,
                    userDTO.getUser_id(),
                    true,
                    desc,
                    false
            );

            boolean ok = db.addWithdrawals(w);
            if (ok) {
                Toast.makeText(requireContext(), "ثبت گردید !", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dlg.dismiss();
            } else {
                Toast.makeText(requireContext(), "تکرار کوشش !", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dlg.dismiss());
    }

    private void showUserEditDialog(UserDTO userDTO, String shortName) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dlg.show();

        Button btnSave = dialogView.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.customer_update));
        btnSave.setText(getString(R.string.update));

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogView.findViewById(R.id.outItem4);

        out1.setHint(getString(R.string.userName));
        out2.setHint(getString(R.string.userMobile));
        out3.setHint(getString(R.string.userAdders));
        out4.setHint(getString(R.string.productDescription));

        TextInputEditText t1 = dialogView.findViewById(R.id.txtItem1);
        TextInputEditText t2 = dialogView.findViewById(R.id.txtItem2);
        TextInputEditText t3 = dialogView.findViewById(R.id.txtItem3);
        TextInputEditText t4 = dialogView.findViewById(R.id.txtItem4);

        t2.setInputType(InputType.TYPE_CLASS_PHONE);
        t3.setInputType(InputType.TYPE_CLASS_TEXT);

        users u = db.getUserById(userDTO.getUser_id());
        if (u != null) {
            t1.setText(nullToEmpty(u.getName()));
            t2.setText(nullToEmpty(u.getMobile()));
            t3.setText(nullToEmpty(u.getAddress()));
            t4.setText(nullToEmpty(u.getDescription()));
        }

        btnSave.setOnClickListener(v -> {
            String newName = safeText(t1);
            String newMobile = safeText(t2);
            String newAddress = safeText(t3);
            String newDesc = safeText(t4);

            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "نام خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            String now = safeNowDbDateTime();

            users updated = new users(
                    userDTO.getUser_id(),
                    newName,
                    newMobile,
                    newAddress,
                    now,
                    now,
                    true,
                    newDesc,
                    false
            );

            boolean ok = db.updateUser(updated);
            if (ok) {
                Toast.makeText(requireContext(), "ویرایش گردید !", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dlg.dismiss();
            } else {
                Toast.makeText(requireContext(), "قبلا موجود ایست!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dlg.dismiss());
    }

    private void showUserDeleteDialog(UserDTO userDTO, String shortName, String sideTitle) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dlg.show();

        Button btnDelete = dialogView.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogView.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.delete) + " شخص");
        btnDelete.setText(getString(R.string.delete));

        LinearLayout hide = dialogView.findViewById(R.id.outItemHide1);
        hide.setVisibility(View.VISIBLE);

        TextInputLayout out1 = dialogView.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogView.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogView.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogView.findViewById(R.id.outItem4);

        out1.setVisibility(View.GONE);
        out2.setVisibility(View.GONE);
        out3.setVisibility(View.GONE);
        out4.setVisibility(View.GONE);

        ((TextView) dialogView.findViewById(R.id.txtItemHide1)).setText(shortName);
        ((TextView) dialogView.findViewById(R.id.txtItemHide2)).setText(sideTitle);

        btnDelete.setOnClickListener(v -> {
            users removed = db.getUserById(userDTO.getUser_id());
            if (removed == null) {
                Toast.makeText(requireContext(), "کاربر پیدا نشد", Toast.LENGTH_SHORT).show();
                return;
            }
            removed.setDeleteStatus(true);

            boolean ok = db.updateUser(removed);
            if (ok) {
                Toast.makeText(requireContext(), "حذف شد", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dlg.dismiss();
            } else {
                Toast.makeText(requireContext(), "حذف نشد", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dlg.dismiss());
    }

    // ---------- PDF ----------

    private String buildUsersPdfHtml(String title, List<UserDTO> data, @Nullable String query) {

        // ---- totals for header ----
        double totalDeposit = 0.0;     // جمله رسید
        double totalWithdraw = 0.0;    // جمله باقی

        double totalDebtorAbs = 0.0;   // net<0 => |net| toplamı (net!=0 olanlar)
        double totalCreditor = 0.0;    // net>0 => net toplamı (net!=0 olanlar)

        int debtorCount = 0;
        int creditorCount = 0;

        // ---- rows (net != 0 only) ----
        final List<UserRow> rows = new ArrayList<>();

        if (data != null) {
            for (UserDTO u : data) {
                if (u == null) continue;

                double dep = u.getDepositAmount();
                double wit = u.getWithdrawalsAmount();
                double net = dep - wit;

                totalDeposit += dep;
                totalWithdraw += wit;

                // PDF listesine net=0 olanlar girmez
                if (Math.abs(net) < 0.000001) continue;

                boolean isDebtor = net < 0;
                double absNet = Math.abs(net);

                if (isDebtor) {
                    debtorCount++;
                    totalDebtorAbs += absNet;
                } else {
                    creditorCount++;
                    totalCreditor += net;
                }

                rows.add(new UserRow(u, net, absNet, isDebtor));
            }
        }

        // ---- sorting: debtors first by absNet desc, then creditors by net desc ----
        rows.sort((a, b) -> {
            if (a.isDebtor != b.isDebtor) return a.isDebtor ? -1 : 1;   // قرضدار بالا
            if (a.isDebtor) return Double.compare(b.absNet, a.absNet);   // قرضدار: |net| بزرگتر بالا
            return Double.compare(b.net, a.net);                         // طلب: net بزرگتر بالا
        });


        String q = (query == null) ? "" : query.trim();

        // ---- html (stable layout) ----
        StringBuilder sb = new StringBuilder(16_384);

        sb.append("<html dir='rtl' lang='fa'><head><meta charset='utf-8'/>")
                .append("<style>")
                .append("body{font-family:sans-serif;padding:16px;direction:rtl;}")
                .append("h2{margin:0 0 10px 0;font-size:18px;}")
                .append(".meta{margin:8px 0 12px 0;font-size:12px;line-height:1.7;color:#444;}")
                .append(".chips{margin-bottom:6px;}")
                .append(".badge{display:inline-block;padding:2px 8px;border:1px solid #ddd;border-radius:999px;background:#fafafa;color:#333;font-size:11px;margin-left:6px;}")
                .append(".grid{display:flex;gap:10px;flex-wrap:wrap;}")
                .append(".card{border:1px solid #e6e6e6;background:#fcfcfc;border-radius:10px;padding:10px 12px;min-width:220px;flex:1;}")
                .append(".row{display:flex;justify-content:space-between;gap:10px;padding:3px 0;}")
                .append(".k{font-weight:600;color:#555;}")
                .append(".v{font-weight:700;color:#111;}")
                // table stable
                .append("table{width:100%;border-collapse:collapse;font-size:12px;table-layout:fixed;}")
                .append("thead{display:table-header-group;}")
                .append("th,td{border:1px solid #ddd;padding:8px;text-align:right;vertical-align:top;word-break:break-word;}")
                .append("th{background:#f3f3f3;font-weight:700;}")
                .append("th:nth-child(1),td:nth-child(1){width:32px;text-align:center;}")
                .append("th:nth-child(3),td:nth-child(3){width:110px;direction:ltr;text-align:left;}") // موبایل
                .append("th:nth-child(4),td:nth-child(4){width:120px;}") // نتیجه
                .append("th:nth-child(5),td:nth-child(5){width:70px;}")  // وضعیت
                .append(".neg,.pos{color:#000000;font-weight:700;}")
                .append("</style></head><body>");

        sb.append("<h2>").append(esc(title)).append("</h2>");

        // ---- meta ----
        sb.append("<div class='meta'>")
                .append("<div class='chips'>")
                .append("<span class='badge'>گزارش</span>")
                .append("<span class='badge'>اشخاص</span>")
                .append("</div>")
                .append("<div class='grid'>")


                // Card 3 (net!=0 kişiler üzerinden net borç/alacak toplamı)
                .append("<div class='card'>")
                .append("<div class='row'><span class='k'>جمع قرض (خالص)</span><span class='v'>")
                .append(esc(df.format(totalDebtorAbs))).append("</span></div>")
                .append("<div class='row'><span class='k'>جمع طلب (خالص)</span><span class='v'>")
                .append(esc(df.format(totalCreditor))).append("</span></div>")
                .append("<div class='row'><span class='k'>تعداد (نمایش)</span><span class='v'>")
                .append(rows.size()).append("</span></div>")
                .append("</div>")

                .append("</div>") // grid
                .append("</div>"); // meta

        // ---- table ----
        sb.append("<table><thead><tr>")
                .append("<th>#</th>")
                .append("<th>نام</th>")
                .append("<th>موبایل</th>")
                .append("<th>نتیجه</th>")
                .append("<th>وضعیت</th>")
                .append("</tr></thead><tbody>");

        int rowNo = 0;
        for (UserRow r : rows) {
            rowNo++;

            String status = r.isDebtor ? "قرضدار" : "طلب";
            String cls = r.isDebtor ? "neg" : "pos";
            String resultMoney = df.format(r.absNet); // her zaman pozitif yaz

            sb.append("<tr>")
                    .append("<td>").append(rowNo).append("</td>")
                    .append("<td>").append(esc(nullToEmpty(r.u.getName()))).append("</td>")
                    .append("<td>").append(esc(nullToEmpty(r.u.getMobile()))).append("</td>")
                    .append("<td class='").append(cls).append("'>").append(esc(resultMoney)).append("</td>")
                    .append("<td class='").append(cls).append("'>").append(esc(status)).append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody></table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    // ---- helper row model ----
    private static class UserRow {
        final UserDTO u;
        final double net;
        final double absNet;
        final boolean isDebtor;

        UserRow(UserDTO u, double net, double absNet, boolean isDebtor) {
            this.u = u;
            this.net = net;
            this.absNet = absNet;
            this.isDebtor = isDebtor;
        }
    }

    @Override
    public void onFragmentUpdated() {
        refreshAndRender();
    }

    // ---------- helpers ----------

    private static <T> List<T> safeList(@Nullable List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private static String nullToEmpty(@Nullable String s) {
        return s == null ? "" : s;
    }

    private static String safeText(@Nullable TextInputEditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private static String safeShort(String s, int maxLen) {
        if (s == null) return "";
        if (maxLen <= 0) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    private static BigDecimal safeMoneyBD(@Nullable TextInputEditText et) {
        try {
            String s = safeText(et);
            if (s.isEmpty()) return null;
            BigDecimal bd = new BigDecimal(s);
            return bd.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String safeNowDbDateTime() {
        try {
            return AppDate.nowDbDateTime();
        } catch (Exception ignore) {
            // fallback: minimum safe
            return "";
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

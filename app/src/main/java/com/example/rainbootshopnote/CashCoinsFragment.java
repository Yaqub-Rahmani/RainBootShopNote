package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rainbootshopnote.Dto.CashDTO;
import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.deposits;
import com.example.rainbootshopnote.entities.withdrawals;
import com.example.rainbootshopnote.myAdapters.CashAdapter;
import com.example.rainbootshopnote.utils.PdfExportHelper;
import com.example.rainbootshopnote.utils.date.AppDate;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CashCoinsFragment extends Fragment implements MainActivity.FragmentUpdater {

    private DBHelper dbHelper;
    private CashAdapter cashAdapter;

    // UI refs
    private TextView shortInfoTitle;

    // State
    private String searchQuery = "";
    private String filterFrom = null; // "yyyy-MM-dd"
    private String filterTo = null;   // "yyyy-MM-dd"
    private int flowTypeIndex = 0;    // 0=all, 1=out(باقی), 2=in(رسید)

    // Data
    private List<CashDTO> filteredListDisplay = new ArrayList<>();

    // PDF options flags (senin kodundan bırakıldı)
    //private boolean useSolar = false;       // true => 1404-...  false => 2025-...
    private boolean useMonthName = false;   // true => جوزا     false => 04
    private boolean showSolar = false; // false=میلادی, true=هجری شمسی



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_coins, container, false);

        // ListView + short info
        ListView cashListView = view.findViewById(R.id.cash_info_listView);
        shortInfoTitle = view.findViewById(R.id.txtItemShortInfo);

        // Header include
        View header = view.findViewById(R.id.includeHeader);
        TextView txtHeaderTitle = header.findViewById(R.id.txtHeaderTitle);

        View searchContainer = header.findViewById(R.id.search_container);
        EditText etHeaderSearch = header.findViewById(R.id.etHeaderSearch);

        View btnHeaderSearch = header.findViewById(R.id.btnHeaderSearch);
        View btnHeaderFilter = header.findViewById(R.id.btnHeaderFilter);
        View btnHeaderPdf = header.findViewById(R.id.btnHeaderPdf);
//        btnHeaderFilter.setVisibility(View.GONE);

        // Title
        txtHeaderTitle.setText(getString(R.string.cashFragmentTitle));

        // DB
        dbHelper = new DBHelper(requireContext());

        // Adapter
        cashAdapter = new CashAdapter(requireContext(), filteredListDisplay);
        cashListView.setAdapter(cashAdapter);

        // Restore search state
        etHeaderSearch.setText(searchQuery == null ? "" : searchQuery);

        // First render
        refreshAndRender();

        // Search icon toggle
        btnHeaderSearch.setOnClickListener(v -> {
            if (searchContainer.getVisibility() == View.VISIBLE) {
                searchContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up));
                searchContainer.setVisibility(View.GONE);
            } else {
                searchContainer.setVisibility(View.VISIBLE);
                searchContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));
                etHeaderSearch.requestFocus();
            }
        });

        // Search text watcher
        etHeaderSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = (s == null) ? "" : s.toString().trim();
                refreshAndRender();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter bottom sheet
        String[] labels = new String[]{"همه", "بقایات", "رسیده ها"};
        btnHeaderFilter.setOnClickListener(v -> {
            FilterBottomSheetDialogFragment sheet =
                    FilterBottomSheetDialogFragment.newInstance(filterFrom, filterTo, flowTypeIndex, true, labels, showSolar);

            sheet.setOnFilterAppliedListener(new FilterBottomSheetDialogFragment.OnFilterAppliedListener() {
                @Override
                public void onFilterApplied(@Nullable String fromDate, @Nullable String toDate, int flowIndex, boolean showSolarSelected) {

                    filterFrom = fromDate;     // her zaman Gregorian yyyy-MM-dd
                    filterTo = toDate;
                    flowTypeIndex = flowIndex;

                    // ✅ kritik: seçim burada state’e yazılmalı
                    showSolar = showSolarSelected;

                    // ✅ kritik: listeyi anında yeniden çiz
                    if (cashAdapter != null) cashAdapter.setShowSolar(showSolar);

                    // ✅ PDF için de aynı showSolar kullanılacak
                    refreshAndRender();
                }

                @Override
                public void onFilterCleared() {
                    filterFrom = null;
                    filterTo = null;
                    flowTypeIndex = 0;

                    showSolar = false; // veya istediğin default
                    if (cashAdapter != null) cashAdapter.setShowSolar(showSolar);

                    refreshAndRender();
                }
            });

            sheet.show(getParentFragmentManager(), "cash_filter");
        });


        // PDF
        btnHeaderPdf.setOnClickListener(v -> {
            // Ekranda görünen (varsayılan/filtre uygulanmış) listeyi PDF yap
            List<CashDTO> data = new ArrayList<>(filteredListDisplay);

            String title = getString(R.string.cashFragmentTitle);
            String html = buildCashPdfHtml(title, data, showSolar, true);

            PdfExportHelper.printHtml(
                    requireContext(),
                    title,
                    html
            );
        });

//        btnHeaderPdf.setOnClickListener(v -> {
//            PdfOptionsDialogFragment dlg = new PdfOptionsDialogFragment();
//            dlg.setOnPdfConfirmedListener(exportFiltered -> {
//                List<CashDTO> data = exportFiltered
//                        ? new ArrayList<>(filteredListDisplay)
//                        : safeList(dbHelper.getAllCashDTO());
//
//                String title = getString(R.string.cashFragmentTitle);
////                String html = buildCashPdfHtml(title, data, useSolar, useMonthName);
//                String html = buildCashPdfHtml(title, data, showSolar, /*useMonthName*/ true);
//
//                com.example.rainbootshopnote.utils.PdfExportHelper.printHtml(
//                        requireContext(),
//                        title,
//                        html
//                );
//            });
//            dlg.show(getParentFragmentManager(), "pdf_options");
//        });

        // Item click
        cashListView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < filteredListDisplay.size()) {
                CashDTO selected = filteredListDisplay.get(position);
                if (selected != null) showDetails(selected);
            }
        });

        return view;
    }

    /**
     * MainActivity.FragmentUpdater
     * Update/silme/hesaplaşma sonrası burası çağrılıyor.
     * Tek bir yerden refresh ederek crash riskini bitiriyoruz.
     */
    @Override
    public void onFragmentUpdated() {
        refreshAndRender();
    }

    /**
     * Eski kod "updateData()" çağırıyor. Dışarıdan çağıran yerler bozulmasın diye bıraktım.
     * Artık güvenli şekilde aynı işi yapar.
     */
    private void updateData() {
        refreshAndRender();
    }

    private void refreshAndRender() {
        // Fragment detach olmuşsa UI update yapma
        if (!isAdded()) return;

        String s = (searchQuery == null) ? "" : AppDate.toLatinDigits(searchQuery).trim().toLowerCase(Locale.US);

        String from = (filterFrom == null) ? null : AppDate.toLatinDigits(filterFrom).trim();
        String to = (filterTo == null) ? null : AppDate.toLatinDigits(filterTo).trim();
        if (from != null && from.isEmpty()) from = null;
        if (to != null && to.isEmpty()) to = null;

        // DB-level filtering (senin mevcut helper’ına göre)
        List<CashDTO> result = dbHelper.getCashDTOFiltered(s, from, to, flowTypeIndex, null);
        filteredListDisplay = safeList(result);

        // Adapter update
        if (cashAdapter != null) {
            cashAdapter.updateList(filteredListDisplay);
        }

        // Short info (SUM)
        double inTotal = 0;   // toplam ödenen (رسیده ها / deposit)
        double outTotal = 0;  // toplam borç (باقی‌ها / withdrawal)

        for (CashDTO x : filteredListDisplay) {
            if (x == null) continue;
            if (!x.isStatus()) continue; // senin mevcut kuralın: sadece status=true dahil

            double p = x.getPrice(); // CashDTO.price = tutar

            if (x.isDepositOrWithdrawal()) inTotal += p;
            else outTotal += p;
        }

        updateShortInfoTitle(inTotal, outTotal, flowTypeIndex);

    }

    private List<CashDTO> safeList(@Nullable List<CashDTO> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private void showDetails(CashDTO cashDTO) {
        if (!isAdded()) return;

        LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
        View dialogView = dialogInflater.inflate(R.layout.item_more_option, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        String nameShortTitleText = cashDTO.isDepositOrWithdrawal() ? " رسید" : " باقی";

        Button itemEditShow = dialogView.findViewById(R.id.btn_item_edit);
        Button itemMoreShow = dialogView.findViewById(R.id.btn_item_more);
        Button itemStatusShow = dialogView.findViewById(R.id.btn_item_status);
        Button itemDeleteShow = dialogView.findViewById(R.id.btn_item_delete);

        itemStatusShow.setText(nameShortTitleText.trim() + " " + getResources().getString(R.string.save));

        itemStatusShow.setVisibility(cashDTO.isStatus() ? View.VISIBLE : View.GONE);
        itemEditShow.setVisibility(cashDTO.isStatus() ? View.VISIBLE : View.GONE);

        // EDIT
        itemEditShow.setOnClickListener(v -> {
            dialog.dismiss();
            showEditDialog(cashDTO, nameShortTitleText);
        });

        // MORE (safe parse)
        itemMoreShow.setOnClickListener(v -> {
            dialog.dismiss();
            showMoreDialogSafe(cashDTO, nameShortTitleText);
        });

        // STATUS (hesaplaşma)
        itemStatusShow.setOnClickListener(v -> {
            dialog.dismiss();
            showStatusDialog(cashDTO, nameShortTitleText);
        });

        // DELETE
        itemDeleteShow.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteDialog(cashDTO, nameShortTitleText);
        });
    }

    private void showEditDialog(CashDTO cashDTO, String nameShortTitleText) {
        if (!isAdded()) return;

        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEditCreate = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEditCreate.show();

        Button submitBtn = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button cancelBtn = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(getResources().getString(R.string.update) + nameShortTitleText);
        submitBtn.setText(getResources().getString(R.string.update));

        LinearLayout txtLayoutHide1 = dialogViewEdit.findViewById(R.id.outItemHide1);
        txtLayoutHide1.setVisibility(View.VISIBLE);

        String productName = cashDTO.getName() == null ? "" : cashDTO.getName();
        String shortName = (productName.length() > 25) ? productName.substring(0, Math.min(30, productName.length())) : productName;

        TextInputLayout txtLayout1 = dialogViewEdit.findViewById(R.id.outItem1);
        TextInputLayout txtLayout2 = dialogViewEdit.findViewById(R.id.outItem2);
        TextInputLayout txtLayout3 = dialogViewEdit.findViewById(R.id.outItem3);
        TextInputLayout txtLayout4 = dialogViewEdit.findViewById(R.id.outItem4);
        txtLayout2.setVisibility(View.GONE);

        txtLayout1.setHint(getResources().getString(R.string.userName));
        txtLayout3.setHint(getResources().getString(R.string.amount));
        txtLayout4.setHint(getResources().getString(R.string.productDescription));

        TextInputEditText txtItem1 = dialogViewEdit.findViewById(R.id.txtItem1);
        TextInputEditText txtItem3 = dialogViewEdit.findViewById(R.id.txtItem3);
        TextInputEditText txtItem4 = dialogViewEdit.findViewById(R.id.txtItem4);

        TextView txtItemHide1 = dialogViewEdit.findViewById(R.id.txtItemHide1);
        TextView txtItemHide2 = dialogViewEdit.findViewById(R.id.txtItemHide2);

        txtItemHide1.setText(shortName);
        txtItemHide2.setVisibility(View.GONE);

        txtItem1.setText(nullToEmpty(cashDTO.getPayUser()));
        txtItem3.setText(String.valueOf(cashDTO.getPrice()));
        txtItem4.setText(nullToEmpty(cashDTO.getDescription()));

        submitBtn.setOnClickListener(v -> {
            String iName = getTextSafe(txtItem1);
            String iDescription = getTextSafe(txtItem4);

            float iPrice;
            try {
                BigDecimal priceBD = new BigDecimal(getTextSafe(txtItem3));
                priceBD = priceBD.setScale(2, RoundingMode.HALF_UP);
                iPrice = priceBD.floatValue();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "مبلغ خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            DBHelper db = new DBHelper(requireContext());
            boolean success;

            try {
                if (!cashDTO.isDepositOrWithdrawal()) {
                    withdrawals w = db.withdrawalGetByID(cashDTO.getItemId());
                    if (w == null) throw new IllegalStateException("withdrawal null");
                    w.setName(iName);
                    w.setAmount(iPrice);
                    w.setDescription(iDescription);
                    success = db.updateWithdrawals(w);
                } else {
                    deposits d = db.depositGetByID(cashDTO.getItemId());
                    if (d == null) throw new IllegalStateException("deposit null");
                    d.setName(iName);
                    d.setAmount(iPrice);
                    d.setDescription(iDescription);
                    success = db.updateDeposit(d);
                }
            } catch (Exception ex) {
                success = false;
            }

            if (success) {
                Toast.makeText(requireContext(), "ثبت گردید !", Toast.LENGTH_SHORT).show();
                refreshAndRender();
            } else {
                Toast.makeText(requireContext(), "تکرار کوشش کنید !", Toast.LENGTH_SHORT).show();
            }

            dialogEditCreate.dismiss();
        });

        cancelBtn.setOnClickListener(v -> dialogEditCreate.dismiss());
    }

    private void showStatusDialog(CashDTO cashDTO, String nameShortTitleText) {
        if (!isAdded()) return;

        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEditCreate = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEditCreate.show();

        Button okBtn = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button cancelBtn = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(nameShortTitleText + " " + getResources().getString(R.string.statusSubmit));
        okBtn.setText(getResources().getString(R.string.statusSubmit));

        LinearLayout txtLayoutHide1 = dialogViewEdit.findViewById(R.id.outItemHide1);
        txtLayoutHide1.setVisibility(View.VISIBLE);

        // Hide inputs
        dialogViewEdit.findViewById(R.id.outItem1).setVisibility(View.GONE);
        dialogViewEdit.findViewById(R.id.outItem2).setVisibility(View.GONE);
        dialogViewEdit.findViewById(R.id.outItem3).setVisibility(View.GONE);
        dialogViewEdit.findViewById(R.id.outItem4).setVisibility(View.GONE);

        TextView txtItemHide1 = dialogViewEdit.findViewById(R.id.txtItemHide1);
        TextView txtItemHide2 = dialogViewEdit.findViewById(R.id.txtItemHide2);

        txtItemHide1.setText(nullToEmpty(cashDTO.getPayUser()));
        txtItemHide2.setText(String.valueOf(cashDTO.getPrice()) + nameShortTitleText);

        okBtn.setOnClickListener(v -> {
            DBHelper db = new DBHelper(requireContext());
            boolean success;

            try {
                if (!cashDTO.isDepositOrWithdrawal()) {
                    withdrawals w = db.withdrawalGetByID(cashDTO.getItemId());
                    if (w == null) throw new IllegalStateException("withdrawal null");
                    w.setStatus(false);
                    success = db.updateWithdrawals(w);
                } else {
                    deposits d = db.depositGetByID(cashDTO.getItemId());
                    if (d == null) throw new IllegalStateException("deposit null");
                    d.setStatus(false);
                    success = db.updateDeposit(d);
                }
            } catch (Exception ex) {
                success = false;
            }

            Toast.makeText(requireContext(), success ? "حسابی شد" : "حسابی نشد", Toast.LENGTH_SHORT).show();
            if (success) refreshAndRender();
            dialogEditCreate.dismiss();
        });

        cancelBtn.setOnClickListener(v -> dialogEditCreate.dismiss());
    }

    private void showDeleteDialog(CashDTO cashDTO, String nameShortTitleText) {
        if (!isAdded()) return;

        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEditCreate = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEditCreate.show();

        Button okBtn = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button cancelBtn = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(getResources().getString(R.string.delete) + " " + nameShortTitleText);
        okBtn.setText(getResources().getString(R.string.delete));

        LinearLayout txtLayoutHide1 = dialogViewEdit.findViewById(R.id.outItemHide1);
        txtLayoutHide1.setVisibility(View.VISIBLE);

        // Hide inputs
        dialogViewEdit.findViewById(R.id.outItem1).setVisibility(View.GONE);
        dialogViewEdit.findViewById(R.id.outItem2).setVisibility(View.GONE);
        dialogViewEdit.findViewById(R.id.outItem3).setVisibility(View.GONE);
        dialogViewEdit.findViewById(R.id.outItem4).setVisibility(View.GONE);

        TextView txtItemHide1 = dialogViewEdit.findViewById(R.id.txtItemHide1);
        TextView txtItemHide2 = dialogViewEdit.findViewById(R.id.txtItemHide2);

        txtItemHide1.setText(nullToEmpty(cashDTO.getPayUser()));
        txtItemHide2.setText(String.valueOf(cashDTO.getPrice()) + " " + nameShortTitleText);

        okBtn.setOnClickListener(v -> {
            DBHelper db = new DBHelper(requireContext());
            boolean success;

            try {
                if (!cashDTO.isDepositOrWithdrawal()) {
                    withdrawals w = db.withdrawalGetByID(cashDTO.getItemId());
                    if (w == null) throw new IllegalStateException("withdrawal null");
                    w.setDeleteStatus(true);
                    success = db.updateWithdrawals(w);
                } else {
                    deposits d = db.depositGetByID(cashDTO.getItemId());
                    if (d == null) throw new IllegalStateException("deposit null");
                    d.setDeleteStatus(true);
                    success = db.updateDeposit(d);
                }
            } catch (Exception ex) {
                success = false;
            }

            Toast.makeText(requireContext(), success ? "حذف شد" : "حذف نشد", Toast.LENGTH_SHORT).show();
            if (success) refreshAndRender();
            dialogEditCreate.dismiss();
        });

        cancelBtn.setOnClickListener(v -> dialogEditCreate.dismiss());
    }

    private void showMoreDialogSafe(CashDTO cashDTO, String nameShortTitleText) {
        if (!isAdded()) return;

        String raw = cashDTO.getCreateDate(); // ideally "yyyy-MM-dd HH:mm:ss"

        // Extract iso day safely
        String isoDay = safeIsoDay(raw); // "yyyy-MM-dd" or null

        // Both calendars
        String dateGreg = "—";
        String dateSolar = "—";
        if (isoDay != null) {
            dateGreg = safeUiDate(isoDay, false); // Gregorian
            dateSolar = safeUiDate(isoDay, true); // Solar
        }

        // Time (12h)
        String timePart = safeUiTime(raw);

        String msg =
                "****************************" +
                        "\n\nاسـم مشتری: " + nullToEmpty(cashDTO.getName()) +
                        "\n\nاسـم شخص: " + nullToEmpty(cashDTO.getPayUser()) +
                        "\n\nمبلغ پرداخت: " + formatMoney(cashDTO.getPrice()) +
                        "\n\nنوع پرداخت: " + nullToEmpty(nameShortTitleText) +
                        "\n\nمیلادی: " + dateGreg +
                        "\n\nهجری شمسی: " + dateSolar +
                        "\n\nساعت: " + timePart +
                        "\n\nنوضیحات: " + nullToEmpty(cashDTO.getDescription()) +
                        "\n\n****************************";

        new AlertDialog.Builder(requireContext())
                .setTitle("درباره " + nameShortTitleText)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    // filtre meta kısmı için tarih-saat (00:00:00 gibi)
    private String safeUiDate(String isoDateTime, boolean useSolar) {
        try {
            String raw = AppDate.toLatinDigits(nullToEmpty(isoDateTime)).trim();
            if (raw.isEmpty()) return "—";
            return AppDate.toUiDateTime(raw, useSolar);
        } catch (Exception e) {
            return "—";
        }
    }

    // createDate içinden saat (12h) üret
    private String safeUiTime(String createDateRaw) {
        try {
            String raw = AppDate.toLatinDigits(nullToEmpty(createDateRaw)).trim();
            if (raw.isEmpty()) return "—";
            return AppDate.toUiTime12h(raw);
        } catch (Exception e) {
            return "—";
        }
    }

    private String safeIsoDay(String rawDateTime) {
        try {
            String raw = AppDate.toLatinDigits(nullToEmpty(rawDateTime)).trim();
            if (raw.length() >= 10) return raw.substring(0, 10); // yyyy-MM-dd
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String getTextSafe(TextInputEditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    // ─────────────────────────────────────────────
    // PDF helpers (senin mevcut koddaki mantık korunuyor)
    // ─────────────────────────────────────────────

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void updateShortInfoTitle(double inTotal, double outTotal, int flowTypeIndex) {
        if (shortInfoTitle == null) return;

        String inText = formatMoney(inTotal);
        String outText = formatMoney(outTotal);

        // 0=همه, 1=باقی‌ها, 2=رسیده ها
        if (flowTypeIndex == 2) {
            shortInfoTitle.setText(" | مجموع رسیدها: " + inText + " | ");
        } else if (flowTypeIndex == 1) {
            shortInfoTitle.setText(" | مجموع باقی‌ها: " + outText + " | ");
        } else {
            shortInfoTitle.setText(" | مجموع رسیدها: " + inText + " | مجموع باقی‌ها: " + outText + " | ");
        }
    }


    private String formatMoney(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private String buildCashPdfHtml(String title, List<CashDTO> data,
                                    boolean useSolar, boolean useMonthName) {

        double totalIn = 0;
        double totalOut = 0;

        for (CashDTO x : data) {
            if (x == null) continue;
            double p = x.getPrice();
            if (x.isDepositOrWithdrawal()) totalIn += p;
            else totalOut += p;
        }

        String typeLabelFa;
        if (flowTypeIndex == 2) typeLabelFa = "رسید‌ها";
        else if (flowTypeIndex == 1) typeLabelFa = "باقی‌ها";
        else typeLabelFa = "همه";

        String reportTitle = esc(title) + " — " + typeLabelFa;

        StringBuilder summary = new StringBuilder();
        if (flowTypeIndex == 2) {
            summary.append("<div class='sumrow'><span class='k'>مجموع رسید‌ها:</span>")
                    .append("<span class='v'>").append(formatMoney(totalIn)).append("</span></div>");
        } else if (flowTypeIndex == 1) {
            summary.append("<div class='sumrow'><span class='k'>مجموع باقی‌ها:</span>")
                    .append("<span class='v'>").append(formatMoney(totalOut)).append("</span></div>");
        } else {
            summary.append("<div class='sumgrid'>")
                    .append("<div class='sumrow'><span class='k'>مجموع رسید‌ها:</span><span class='v'>")
                    .append(formatMoney(totalIn)).append("</span></div>")
                    .append("<div class='sumrow'><span class='k'>مجموع باقی‌ها:</span><span class='v'>")
                    .append(formatMoney(totalOut)).append("</span></div>")
                    .append("<div class='sumrow net'><span class='k'>خالص:</span><span class='v'>")
                    .append(formatMoney(totalIn - totalOut)).append("</span></div>")
                    .append("</div>");
        }

        String fromText = (filterFrom == null || filterFrom.trim().isEmpty())
                ? "—"
                : esc(AppDate.toUiDateTime(filterFrom + " 00:00:00", useSolar));

        String toText = (filterTo == null || filterTo.trim().isEmpty())
                ? "—"
                : esc(AppDate.toUiDateTime(filterTo + " 00:00:00", useSolar));

        StringBuilder sb = new StringBuilder();
        sb.append("<html dir='rtl' lang='fa'><head><meta charset='utf-8'/>")
                .append("<style>")
                .append("body{font-family:sans-serif;padding:16px;direction:rtl;}")
                .append("h2{margin:0 0 8px 0;font-size:18px;}")
                .append(".meta{color:#555;margin:8px 0 12px 0;font-size:12px;line-height:1.6;}")
                .append(".badge{display:inline-block;padding:2px 8px;border:1px solid #ddd;border-radius:999px;background:#fafafa;color:#333;font-size:11px;margin-left:6px;}")
                .append(".summary{border:1px solid #e6e6e6;background:#fcfcfc;border-radius:10px;padding:10px 12px;margin:10px 0 14px 0;}")
                .append(".sumgrid{display:block;}")
                .append(".sumrow{display:flex;justify-content:space-between;gap:12px;padding:4px 0;}")
                .append(".sumrow .k{color:#444;font-weight:600;}")
                .append(".sumrow .v{color:#111;font-weight:700;}")
                .append(".sumrow.net{border-top:1px dashed #ddd;margin-top:6px;padding-top:8px;}")
                .append("table{width:100%;border-collapse:collapse;font-size:12px;}")
                .append("th,td{border:1px solid #ddd;padding:8px;text-align:right;vertical-align:top;}")
                .append("th{background:#f3f3f3;font-weight:700;}")
                .append(".muted{color:#777;}")
                // ✅ Satır numarası kolonu (ilk kolon) dar + ortalı
                .append("th:first-child, td:first-child{width:30px;text-align:center;}")
                .append("</style></head><body>");

        sb.append("<h2>").append(reportTitle).append("</h2>");

        sb.append("<div class='meta'>")
                .append("<span class='badge'>گزارش</span>")
                .append("<span class='badge'>").append("نوع: ").append(esc(typeLabelFa)).append("</span>")
                .append("<br/>")
                .append("جستجو: ").append(esc(searchQuery == null ? "" : searchQuery))
                .append("<br/>")
                .append("از تاریخ: ").append(fromText)
                .append(" &nbsp; | &nbsp; ")
                .append("تا تاریخ: ").append(toText)
                .append("</div>");

        sb.append("<div class='summary'>")
                .append("<div class='muted'>خلاصه</div>")
                .append(summary)
                .append("</div>");

        // ✅ Header'a satır numarası kolonu eklendi
        sb.append("<table><thead><tr>")
                .append("<th>#</th>")
                .append("<th>تاریخ</th>")
                .append("<th>ساعت</th>")
                .append("<th>نام</th>")
                .append("<th>توضیحات</th>")
                .append("<th>مبلغ</th>")
                .append("<th>نوع</th>")
                .append("</tr></thead><tbody>");

        // ✅ Satır numarası sayacı
        int rowNo = 0;

        for (CashDTO x : data) {
            if (x == null) continue;
            rowNo++;

            String rawCreate = x.getCreateDate() == null ? "" : x.getCreateDate();
            String uiDate = esc(AppDate.toUiDate(rawCreate, useSolar));
            String uiTime = esc(AppDate.toUiTime12h(rawCreate));

            String name = esc(nullToEmpty(x.getName()));
            String desc = esc(nullToEmpty(x.getDescription()));
            String amount = formatMoney(x.getPrice());

            boolean isPay = x.isDepositOrWithdrawal();
            String typeFa = isPay ? "رسید" : "باقی";

            sb.append("<tr>")
                    .append("<td>").append(rowNo).append("</td>")
                    .append("<td>").append(uiDate).append("</td>")
                    .append("<td>").append(uiTime).append("</td>")
                    .append("<td>").append(name).append("</td>")
                    .append("<td>").append(desc).append("</td>")
                    .append("<td>").append(amount).append("</td>")
                    .append("<td>").append(typeFa).append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody></table></body></html>");
        return sb.toString();
    }

    // (Opsiyonel) Eğer başka yerde kullanıyorsan diye bıraktım:
    private interface OnDateSetListener {
        void onDateSet(Date date);
    }

    // Tek Date Picker methodu (Optional minDate)
    private void showDatePicker(@Nullable Date minDate, OnDateSetListener listener) {
        if (!isAdded()) return;

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                AlertDialog.THEME_HOLO_LIGHT,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    listener.onDateSet(selectedDate.getTime());
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (minDate != null) {
            datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
        }

        datePickerDialog.show();
    }
}

package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rainbootshopnote.Dto.ProductDTO;
import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.insertProducts;
import com.example.rainbootshopnote.entities.sales;
import com.example.rainbootshopnote.myAdapters.ProductAdapter;
import com.example.rainbootshopnote.utils.PdfExportHelper;
import com.example.rainbootshopnote.utils.date.AppDate;
import com.example.rainbootshopnote.utils.date.DateNormalize;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleFragment extends Fragment implements MainActivity.FragmentUpdater {

    private DBHelper dbHelper;
    private ProductAdapter productAdapter;

    // UI
    private TextView shortInfoTitle;

    // State
    private String searchQuery = "";
    private String filterFrom = null;   // "yyyy-MM-dd" (Gregorian, Latin digits)
    private String filterTo = null;     // "yyyy-MM-dd" (Gregorian, Latin digits)
    private int flowTypeIndex = 0;      // 0=همه, 1=خریداری, 2=فروشات  (sale labels’i buna göre verdik)
    private boolean showSolar = false;  // UI date (adapter + pdf) solar/gregorian

    // Data
    private List<ProductDTO> filteredListDisplay = new ArrayList<>();

    private final SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sale, container, false);

        // List + short info
        ListView listView = root.findViewById(R.id.product_sale_info_listView);
        shortInfoTitle = root.findViewById(R.id.txtItemShortInfo);

        // Header include (aynı CashCoinsFragment)
        View header = root.findViewById(R.id.includeHeader);
        TextView txtHeaderTitle = header.findViewById(R.id.txtHeaderTitle);

        View searchContainer = header.findViewById(R.id.search_container);
        EditText etHeaderSearch = header.findViewById(R.id.etHeaderSearch);

        View btnHeaderSearch = header.findViewById(R.id.btnHeaderSearch);
        View btnHeaderFilter = header.findViewById(R.id.btnHeaderFilter);
        View btnHeaderPdf = header.findViewById(R.id.btnHeaderPdf);

        txtHeaderTitle.setText("خریداری و فروشات");

        dbHelper = new DBHelper(requireContext());

        productAdapter = new ProductAdapter(requireContext(), filteredListDisplay);
        productAdapter.setShowSolar(showSolar);
        listView.setAdapter(productAdapter);

        // restore search
        etHeaderSearch.setText(searchQuery == null ? "" : searchQuery);

        // first render
        refreshAndRender();

        // search toggle
        btnHeaderSearch.setOnClickListener(view1 -> {
            if (searchContainer.getVisibility() == View.VISIBLE) {
                searchContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up));
                searchContainer.setVisibility(View.GONE);
            } else {
                searchContainer.setVisibility(View.VISIBLE);
                searchContainer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down));
                etHeaderSearch.requestFocus();
            }
        });

        // live search
        etHeaderSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = (s == null) ? "" : s.toString().trim();
                refreshAndRender();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter sheet (CashCoinsFragment ile aynı mekanik)
        // labels: 0=همه, 1=خریداری, 2=فروشات
        String[] labels = new String[]{"همه", "خریداری", "فروشات"};
        btnHeaderFilter.setOnClickListener(view1 -> {
            FilterBottomSheetDialogFragment sheetDialog =
                    FilterBottomSheetDialogFragment.newInstance(
                            filterFrom,
                            filterTo,
                            flowTypeIndex,
                            true,
                            labels,
                            showSolar
                    );

            sheetDialog.setOnFilterAppliedListener(new FilterBottomSheetDialogFragment.OnFilterAppliedListener() {
                @Override
                public void onFilterApplied(@Nullable String fromDate, @Nullable String toDate, int flowIndex, boolean showSolarSelected) {
                    filterFrom = fromDate;     // Gregorian yyyy-MM-dd
                    filterTo = toDate;
                    flowTypeIndex = flowIndex;

                    showSolar = showSolarSelected;
                    if (productAdapter != null) productAdapter.setShowSolar(showSolar);

                    refreshAndRender();
                }

                @Override
                public void onFilterCleared() {
                    filterFrom = null;
                    filterTo = null;
                    flowTypeIndex = 0;

                    showSolar = false;
                    if (productAdapter != null) productAdapter.setShowSolar(showSolar);

                    refreshAndRender();
                }
            });

            sheetDialog.show(getParentFragmentManager(), "sale_filter");
        });

        // PDF (model yok: ekranda görünen listeyi basıyoruz) — CashCoinsFragment ile aynı yaklaşım :contentReference[oaicite:1]{index=1}
        btnHeaderPdf.setOnClickListener(view1 -> {
            try {
                List<ProductDTO> data = new ArrayList<>(filteredListDisplay);
                String title = "خریداری و فروشات";
                String html = buildSalePdfHtmlSafe(title, data, showSolar);
                PdfExportHelper.printHtml(
                        requireActivity(),
                        title,
                        html
                );
            } catch (Exception e) {
                Toast.makeText(requireContext(), "PDF خطا: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        // item click => more + delete
        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < filteredListDisplay.size()) {
                ProductDTO selected = filteredListDisplay.get(position);
                if (selected != null) showDetails(selected);
            }
        });

        return root;
    }

    @Override
    public void onFragmentUpdated() {
        refreshAndRender();
    }

    // Eski dış çağrılar bozulmasın diye (CashCoinsFragment’te aynı yaklaşım var) :contentReference[oaicite:2]{index=2}
    private void updateData() {
        refreshAndRender();
    }

    private void refreshAndRender() {
        if (!isAdded()) return;

        String s = (searchQuery == null) ? "" : AppDate.toLatinDigits(searchQuery).trim().toLowerCase(Locale.US);
        String from = normalizeDay(filterFrom);
        String to = normalizeDay(filterTo);

        // Kaynak liste (DB’den)
        List<ProductDTO> all = safeList(dbHelper.getProductDTOList());

        // 1) search
        List<ProductDTO> r1 = new ArrayList<>();
        for (ProductDTO p : all) {
            if (p == null) continue;
            String name = (p.getName() == null) ? "" : p.getName().toLowerCase(Locale.US);
            if (s.isEmpty() || name.contains(s)) r1.add(p);
        }

        // 2) date range (createDate "yyyy-MM-dd HH:mm:ss" olabilir -> sadece günü alıyoruz)
        List<ProductDTO> r2 = new ArrayList<>();
        for (ProductDTO p : r1) {
            if (p == null) continue;

            String isoDay = DateNormalize.extractIsoDay(p.getCreateDate()); // CashAdapter aynı yaklaşımı kullanıyor :contentReference[oaicite:3]{index=3}
            if (isoDay == null || isoDay.trim().isEmpty()) continue;

            if (isInRange(isoDay, from, to)) r2.add(p);
        }

        // 3) flowType: 0=all, 1=insert(خریداری), 2=sale(فروشات)
        List<ProductDTO> r3 = new ArrayList<>();
        for (ProductDTO p : r2) {
            if (p == null) continue;

            boolean isSale = p.isSaleOrInsert();
            if (flowTypeIndex == 2) {
                if (isSale) r3.add(p);
            } else if (flowTypeIndex == 1) {
                if (!isSale) r3.add(p);
            } else {
                r3.add(p);
            }
        }

        filteredListDisplay = r3;

        if (productAdapter != null) {
            productAdapter.updateList(filteredListDisplay);
        }



        updateShortInfo(filteredListDisplay, flowTypeIndex);
    }

    private boolean isInRange(String isoDay, @Nullable String from, @Nullable String to) {
        // from/to null => açık aralık
        try {
            Date d = dayFmt.parse(isoDay);
            if (d == null) return false;

            if (from != null) {
                Date f = dayFmt.parse(from);
                if (f != null && d.before(f)) return false;
            }

            if (to != null) {
                Date t = dayFmt.parse(to);
                if (t != null && d.after(t)) return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private String normalizeDay(@Nullable String day) {
        if (day == null) return null;
        String x = AppDate.toLatinDigits(day).trim();
        return x.isEmpty() ? null : x;
    }

    private List<ProductDTO> safeList(@Nullable List<ProductDTO> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private void updateShortInfo(List<ProductDTO> data, int flowIndex) {
        if (shortInfoTitle == null) return;

        double totalSaleAmount = 0;   // satış toplamı
        double totalBuyAmount = 0;    // alış toplamı
        double totalProfit = 0;       // kazanç (win toplamı, sadece satış)
        double totalQty = 0;

        for (ProductDTO p : data) {
            if (p == null) continue;

            double lineAmount = p.getQuantity() * p.getPrice();
            totalQty += p.getQuantity();

            if (p.isSaleOrInsert()) {
                // satış
                totalSaleAmount += lineAmount;
                totalProfit += p.getWin();
            } else {
                // alış
                totalBuyAmount += lineAmount;
            }
        }

        String qtyText = String.format(Locale.US, "%.0f", totalQty);
        String saleText = String.format(Locale.US, "%.2f", totalSaleAmount);
        String profitText = String.format(Locale.US, "%.2f", totalProfit);
        String buyText = String.format(Locale.US, "%.2f", totalBuyAmount);

        if (flowIndex == 2) {
            // فقط فروشات
            shortInfoTitle.setText(" | تعداد: " + qtyText +
                    " | مجموع فروش: " + saleText +
                    " | مجموع مفاد: " + profitText + " | ");
        } else if (flowIndex == 1) {
            // فقط خریداری
            shortInfoTitle.setText(" | تعداد: " + qtyText +
                    " | مجموع خرید: " + buyText + " | ");
        } else {
            // همه
            shortInfoTitle.setText(//" | تعداد: " + qtyText +
                    " | فروش: " + saleText +
                    " | خرید: " + buyText +
                    " | مفاد: " + profitText + " | ");
        }
    }

    private void showDetails(ProductDTO productDTO) {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.item_more_option, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        String typeFa = productDTO.isSaleOrInsert() ? "فروش" : "خرید";

        View btnMore = dialogView.findViewById(R.id.btn_item_more);
        View btnDelete = dialogView.findViewById(R.id.btn_item_delete);

        // Bu ekranda edit/status kapalı (old SaleFragment’te böyleydi) :contentReference[oaicite:4]{index=4}
        dialogView.findViewById(R.id.btn_item_edit).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btn_item_status).setVisibility(View.GONE);

        btnMore.setOnClickListener(v -> {
            dialog.dismiss();
            showMoreDialogSafe(productDTO, typeFa);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteDialog(productDTO, typeFa);
        });
    }

    private void showMoreDialogSafe(ProductDTO productDTO, String typeFa) {
        if (!isAdded()) return;

        String raw = productDTO.getCreateDate();

        String isoDay = null;
        if (raw != null && !raw.trim().isEmpty()) {
            isoDay = DateNormalize.extractIsoDay(raw); // "yyyy-MM-dd"
        }

        // Both calendars
        String dateGreg = "—";
        String dateSolar = "—";
        String timePart = "—";

        if (isoDay != null && !isoDay.trim().isEmpty()) {
            // Gregorian: force useSolar=false
            dateGreg = safeUiDate(isoDay, false);
            // Solar: force useSolar=true
            dateSolar = safeUiDate(isoDay, true);
        }

        if (raw != null && !raw.trim().isEmpty()) {
            timePart = safeUiTime(raw);
        }

        double qty = productDTO.getQuantity();
        double price = productDTO.getPrice();
        double amount = qty * price;

        String amountLine = "\n\nمبلغ مجموع: " + formatMoney(amount);

        String msg =
                "****************************" +
                        "\n\nاسـم جنس: " + nullToEmpty(productDTO.getName()) +
                        "\n\nنوع: " + typeFa +
                        "\n\nقیمت (" + typeFa + "): " + formatMoney(price) +
                        "\n\nتعداد: " + String.format(Locale.US, "%.0f", qty) +
                        amountLine +
                        "\n\nمیلادی: " + dateGreg +
                        "\n\nهجری شمسی: " + dateSolar +
                        "\n\nساعت: " + timePart +
                        "\n\nتوضیحات: " + nullToEmpty(productDTO.getDescription()) +
                        "\n\n****************************";

        new AlertDialog.Builder(requireContext())
                .setTitle("درباره " + typeFa)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }


    private void showDeleteDialog(ProductDTO productDTO, String typeFa) {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        TextView txtTitle = dialogView.findViewById(R.id.txtTitle);
        View btnOk = dialogView.findViewById(R.id.SaveProductSubmit);
        View btnCancel = dialogView.findViewById(R.id.CancelProductSubmit);

        txtTitle.setText(getResources().getString(R.string.delete) + " " + typeFa);

        // Hide inputs, show only summary (old SaleFragment delete ekranına uyumlu) :contentReference[oaicite:5]{index=5}
        dialogView.findViewById(R.id.outItemHide1).setVisibility(View.VISIBLE);
        dialogView.findViewById(R.id.outItem1).setVisibility(View.GONE);
        dialogView.findViewById(R.id.outItem2).setVisibility(View.GONE);
        dialogView.findViewById(R.id.outItem3).setVisibility(View.GONE);
        dialogView.findViewById(R.id.outItem4).setVisibility(View.GONE);

        TextView txtItemHide1 = dialogView.findViewById(R.id.txtItemHide1);
        TextView txtItemHide2 = dialogView.findViewById(R.id.txtItemHide2);

        txtItemHide1.setText(nullToEmpty(productDTO.getName()));
        txtItemHide2.setText(String.valueOf(productDTO.getPrice()) + " " + typeFa);

        btnOk.setOnClickListener(v -> {
            DBHelper db = new DBHelper(requireContext());
            boolean success = false;

            try {
                if (productDTO.isSaleOrInsert()) {
                    sales s = db.getSaleById(productDTO.getItemId());
                    if (s != null) {
                        s.setDeleteStatus(true);
                        success = db.updateSale(s);
                    }
                } else {
                    insertProducts ip = db.getInsertProductById(productDTO.getItemId());
                    if (ip != null) {
                        ip.setDeleteStatus(true);
                        success = db.updateInsertProduct(ip);
                    }
                }
            } catch (Exception ignored) { }

            Toast.makeText(requireContext(), success ? "حذف شد" : "حذف نشد", Toast.LENGTH_SHORT).show();
            if (success) refreshAndRender();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    // ─────────────────────────────────────────────
    // PDF
    // ─────────────────────────────────────────────


    private String buildSalePdfHtmlSafe(String title, List<ProductDTO> data, boolean useSolar) {

        // ─────────────────────────────────────────────
        // Totals (only status=true)
        // ─────────────────────────────────────────────
        int totalQty = 0;
        double totalAmount = 0.0;
        double totalWin = 0.0;

        for (ProductDTO x : data) {
            if (x == null || !x.isStatus()) continue;

            int q = (int) x.getQuantity();
            double p = x.getPrice();

            totalQty += q;
            totalAmount += (q * p);

            if (x.isSaleOrInsert()) totalWin += x.getWin();
        }

        // ─────────────────────────────────────────────
        // Meta labels
        // ─────────────────────────────────────────────
        String typeLabelFa = (flowTypeIndex == 2) ? "فروشات"
                : (flowTypeIndex == 1) ? "خریداری"
                : "همه";

        String fromText = isBlank(filterFrom) ? "—" : esc(safeUiDate(filterFrom + " 00:00:00", useSolar));
        String toText   = isBlank(filterTo)   ? "—" : esc(safeUiDate(filterTo + " 00:00:00", useSolar));

        boolean showWinColumn = (flowTypeIndex == 2);

        // ─────────────────────────────────────────────
        // HTML
        // ─────────────────────────────────────────────
        StringBuilder sb = new StringBuilder(16_384);

        sb.append("<html dir='rtl' lang='fa'><head><meta charset='utf-8'/>")
                .append("<style>")
                .append("body{font-family:sans-serif;padding:16px;direction:rtl;}")
                .append("h2{margin:0 0 8px 0;font-size:18px;}")
                .append(".meta{color:#555;margin:8px 0 12px 0;font-size:12px;line-height:1.6;}")
                .append(".badge{display:inline-block;padding:2px 8px;border:1px solid #ddd;border-radius:999px;background:#fafafa;color:#333;font-size:11px;margin-left:6px;}")
                .append(".summary{border:1px solid #e6e6e6;background:#fcfcfc;border-radius:10px;padding:10px 12px;margin:10px 0 14px 0;}")
                .append(".sumrow{display:flex;justify-content:space-between;gap:12px;padding:4px 0;}")
                .append(".sumrow .k{color:#444;font-weight:600;}")
                .append(".sumrow .v{color:#111;font-weight:700;}")
                .append("table{width:100%;border-collapse:collapse;font-size:12px;}")
                .append("th,td{border:1px solid #ddd;padding:8px;text-align:right;vertical-align:top;}")
                .append("th{background:#f3f3f3;font-weight:700;}")
                .append("th:first-child,td:first-child{width:30px;text-align:center;}")
                .append("</style></head><body>");

        // Title + meta
        sb.append("<h2>").append(esc(title)).append(" — ").append(esc(typeLabelFa)).append("</h2>");

        sb.append("<div class='meta'>")
                .append("<span class='badge'>گزارش</span>")
                .append("<span class='badge'>نوع: ").append(esc(typeLabelFa)).append("</span><br/>")
                .append("جستجو: ").append(esc(searchQuery == null ? "" : searchQuery)).append("<br/>")
                .append("از تاریخ: ").append(fromText).append(" &nbsp; | &nbsp; ")
                .append("تا تاریخ: ").append(toText)
                .append("</div>");

        // Summary
        sb.append("<div class='summary'>")
                .append("<div class='sumrow'><span class='k'>تعداد:</span><span class='v'>")
                .append(totalQty).append("</span></div>")
                .append("<div class='sumrow'><span class='k'>مبلغ:</span><span class='v'>")
                .append(formatMoney(totalAmount)).append("</span></div>");

        if (showWinColumn) {
            String winLabel = (totalWin < 0) ? "ضرر" : "مفاد";
            sb.append("<div class='sumrow'><span class='k'>").append(esc(winLabel)).append(":</span><span class='v'>")
                    .append(formatMoney(totalWin)).append("</span></div>");
        }
        sb.append("</div>");

        // Table head
        sb.append("<table><thead><tr>")
                .append("<th>#</th>")
                .append("<th>تاریخ</th>")
                .append("<th>ساعت</th>")
                .append("<th>نام</th>")
                .append("<th>تعداد</th>")
                .append("<th>قیمت</th>")
                .append("<th>مبلغ</th>")
                .append("<th>نوع</th>");

        if (showWinColumn) sb.append("<th>مفاد</th>");

        sb.append("<th>توضیحات</th>")
                .append("</tr></thead><tbody>");

        // Rows
        int rowNo = 0;
        for (ProductDTO x : data) {
            if (x == null || !x.isStatus()) continue;
            rowNo++;

            int q = (int) x.getQuantity();
            double price = x.getPrice();
            double amount = q * price;

            String type = x.isSaleOrInsert() ? "فروش" : "خرید";

            // SAFE date/time:
            // - isoDay: yyyy-MM-dd from createDate (even if "yyyy-MM-dd HH:mm:ss")
            // - uiDate depends on useSolar
            String raw = nullToEmpty(x.getCreateDate());
            String isoDay = safeIsoDay(raw);
            String datePart = esc(safeUiDayOnly(isoDay, useSolar));
            String timePart = esc(safeUiTime(raw)); // uses raw datetime

            sb.append("<tr>")
                    .append("<td>").append(rowNo).append("</td>")
                    .append("<td>").append(datePart).append("</td>")
                    .append("<td>").append(timePart).append("</td>")
                    .append("<td>").append(esc(nullToEmpty(x.getName()))).append("</td>")
                    .append("<td>").append(q).append("</td>")
                    .append("<td>").append(formatMoney(price)).append("</td>")
                    .append("<td>").append(formatMoney(amount)).append("</td>")
                    .append("<td>").append(esc(type)).append("</td>");

            if (showWinColumn) {
                String win = x.isSaleOrInsert() ? formatMoney(x.getWin()) : "—";
                sb.append("<td>").append(esc(win)).append("</td>");
            }

            sb.append("<td>").append(esc(nullToEmpty(x.getDescription()))).append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody></table></body></html>");
        return sb.toString();
    }

    // ---- safe helpers (crash’i bitiren kısım) ----

    private String escSafe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // createDate içinden yyyy-MM-dd gününü çıkart
    private String safeIsoDay(String createDateRaw) {
        try {
            String raw = AppDate.toLatinDigits(nullToEmpty(createDateRaw)).trim();
            if (raw.length() >= 10) return raw.substring(0, 10);
            return "—";
        } catch (Exception e) {
            return "—";
        }
    }

    // sadece gün gösterimi (solar/greg)
    private String safeUiDayOnly(String isoDay, boolean useSolar) {
        try {
            if (isBlank(isoDay) || "—".equals(isoDay)) return "—";
            // Senin projede AppDate.toUiDate varsa onu kullan
            return AppDate.toUiDate(isoDay, useSolar);
        } catch (Exception e) {
            return "—";
        }
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

    private String formatMoney(double v) {
        return String.format(Locale.US, "%.2f", v);
    }


}

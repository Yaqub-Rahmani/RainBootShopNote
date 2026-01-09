package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
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

import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.insertProducts;
import com.example.rainbootshopnote.entities.products;
import com.example.rainbootshopnote.entities.sales;
import com.example.rainbootshopnote.myAdapters.ProductListAdapter;
import com.example.rainbootshopnote.utils.PdfExportHelper;
import com.example.rainbootshopnote.utils.date.AppDate;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements MainActivity.FragmentUpdater {

    private DBHelper db;

    // UI
    private TextView shortInfoTitle;
    private ListView listViewProducts;
    private ProductListAdapter adapter;

    // Data
    private final List<products> allList = new ArrayList<>();
    private final List<products> filteredList = new ArrayList<>();

    // State
    private String searchQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Header include (same IDs as CashCoinsFragment header)
        View header = view.findViewById(R.id.includeHeader);
        TextView txtHeaderTitle = header.findViewById(R.id.txtHeaderTitle);

        View searchContainer = header.findViewById(R.id.search_container);
        TextInputEditText etHeaderSearch = header.findViewById(R.id.etHeaderSearch);

        View btnHeaderSearch = header.findViewById(R.id.btnHeaderSearch);
        View btnHeaderFilter = header.findViewById(R.id.btnHeaderFilter);
        View btnHeaderPdf = header.findViewById(R.id.btnHeaderPdf);

        // Home: filter yok
        btnHeaderFilter.setVisibility(View.GONE);

        // Title
        txtHeaderTitle.setText(getString(R.string.infoHomeFragmentTitle));

        // List + short info
        shortInfoTitle = view.findViewById(R.id.txtItemShortInfo);
        listViewProducts = view.findViewById(R.id.list_view_products);

        // DB
        db = new DBHelper(requireContext());

        // Adapter
        adapter = new ProductListAdapter(requireContext(), filteredList);
        listViewProducts.setAdapter(adapter);

        // İlk yükleme
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

        // Restore search
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

        // PDF: ekranda görünen liste
        btnHeaderPdf.setOnClickListener(v -> {
            List<products> data = new ArrayList<>(filteredList);
            String title = getString(R.string.infoHomeFragmentTitle);
            String html = buildProductsPdfHtml(title, data, searchQuery);

            PdfExportHelper.printHtml(
                    requireContext(),
                    title,
                    html
            );
        });

        // Item click -> detay/işlemler
        listViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                if (position < 0 || position >= filteredList.size()) return;
                products p = filteredList.get(position);
                if (p == null) return;
                showProductActions(p);
            }
        });

        return view;
    }

    private void refreshAndRender() {
        List<products> dbList = safeList(db.getAllProducts());
        allList.clear();
        allList.addAll(dbList);

        applySearchOnly();
    }

    private void applySearchOnly() {
        filteredList.clear();

        String q = (searchQuery == null) ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            for (products p : allList) {
                if (p == null) continue;
                if (p.isDeleteStatus()) continue;
                filteredList.add(p);
            }
        } else {
            for (products p : allList) {
                if (p == null) continue;
                if (p.isDeleteStatus()) continue;

                String name = nullToEmpty(p.getName()).toLowerCase(Locale.ROOT);
                String code = nullToEmpty(p.getCode()).toLowerCase(Locale.ROOT);

                if (name.contains(q) || code.contains(q)) {
                    filteredList.add(p);
                }
            }
        }

        if (adapter != null) adapter.updateList(filteredList);
        updateShortInfo();
    }

    private void updateShortInfo() {
        double capital = 0.0;
        int count = 0;

        for (products p : filteredList) {
            if (p == null) continue;
            if (p.isDeleteStatus()) continue;

            count++;
            capital += (p.getPrice() * p.getQuantity());
        }

        if (shortInfoTitle != null) {
            shortInfoTitle.setText("| سرمایه: " + formatMoney(capital) + " | تعداد: " + count + " |");
        }
    }

    /**
     * Old HomeFragment akışını korur:
     * - Edit
     * - Add count (insertProducts)
     * - Sale (sales)
     * - Delete (soft deleteStatus=true)
     */
    private void showProductActions(products product) {
        LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
        View dialogView = dialogInflater.inflate(R.layout.item_product_more_option, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.show();

        Button btnEdit = dialogView.findViewById(R.id.btn_item_product_edit);
        Button btnAddCount = dialogView.findViewById(R.id.btn_item_product_add_count);
        Button btnSale = dialogView.findViewById(R.id.btn_item_product_sale);
        Button btnDelete = dialogView.findViewById(R.id.btn_item_product_delete);

        TextView txtName = dialogView.findViewById(R.id.txt_item_product_name);

        String productName = nullToEmpty(product.getName());
        String shortName = productName.length() > 30 ? productName.substring(0, 30) : productName;

        String sideTitle =
                String.valueOf(product.getPrice()) + " - قیمت خرید." +
                        "  ___  " +
                        String.valueOf(product.getQuantity()) + " - تعداد موجود.";

        txtName.setText(shortName);

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            showEditDialog(product, shortName);
        });

        btnAddCount.setOnClickListener(v -> {
            dialog.dismiss();
            showAddCountDialog(product, shortName, sideTitle);
        });

        btnSale.setOnClickListener(v -> {
            dialog.dismiss();
            showSaleDialog(product, shortName, sideTitle);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteDialog(product, shortName, sideTitle);
        });
    }

    private void showEditDialog(products product, String shortName) {
        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEdit = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEdit.show();

        Button btnSave = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.product_update));
        btnSave.setText(getString(R.string.update));

        TextInputLayout out1 = dialogViewEdit.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogViewEdit.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogViewEdit.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogViewEdit.findViewById(R.id.outItem4);

        out1.setHint(getString(R.string.productName));
        out2.setHint(getString(R.string.productQuantity));
        out3.setHint(getString(R.string.productPrice));
        out4.setHint(getString(R.string.productDescription));

        TextInputEditText t1 = dialogViewEdit.findViewById(R.id.txtItem1);
        TextInputEditText t2 = dialogViewEdit.findViewById(R.id.txtItem2);
        TextInputEditText t3 = dialogViewEdit.findViewById(R.id.txtItem3);
        TextInputEditText t4 = dialogViewEdit.findViewById(R.id.txtItem4);

        t1.setText(nullToEmpty(product.getName()));
        t2.setText(String.valueOf(product.getQuantity()));
        t3.setText(String.valueOf(product.getPrice()));
        t4.setText(nullToEmpty(product.getDescription()));

        btnSave.setOnClickListener(v -> {
            String newName = safeText(t1);
            Integer newQty = safeInt(t2);
            Double newPrice = safeDouble(t3);
            String newDesc = safeText(t4);

            if (newName.trim().isEmpty()) {
                Toast.makeText(requireContext(), "نام خالی است", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newQty == null || newQty < 0) {
                Toast.makeText(requireContext(), "تعداد درست نیست", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPrice == null || newPrice < 0) {
                Toast.makeText(requireContext(), "قیمت درست نیست", Toast.LENGTH_SHORT).show();
                return;
            }

            products updated = new products(
                    product.getProduct_id(),
                    product.getCode(),
                    newName,
                    newPrice.floatValue(),
                    newQty,
                    true,
                    newDesc,
                    false
            );

            boolean success = db.productUpdate(updated);

            if (success) {
                Toast.makeText(requireContext(), "ویرایش گردید !", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dialogEdit.dismiss();
            } else {
                Toast.makeText(requireContext(), "تکرار کوشش کنید.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialogEdit.dismiss());
    }

    private void showAddCountDialog(products product, String shortName, String sideTitle) {
        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEdit = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEdit.show();

        Button btnSave = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.product_item_count_add));
        btnSave.setText(getString(R.string.save));

        // UI düzeni (old ekrandaki gibi) :contentReference[oaicite:3]{index=3}
        TextInputLayout out1 = dialogViewEdit.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogViewEdit.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogViewEdit.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogViewEdit.findViewById(R.id.outItem4);

        out1.setVisibility(View.GONE);
        out2.setHint(getString(R.string.productQuantity));
        out3.setHint(getString(R.string.product_add_price) + " خرید");
        out4.setHint(getString(R.string.productDescription));

        LinearLayout hide = dialogViewEdit.findViewById(R.id.outItemHide1);
        hide.setVisibility(View.VISIBLE);

        TextView h1 = dialogViewEdit.findViewById(R.id.txtItemHide1);
        TextView h2 = dialogViewEdit.findViewById(R.id.txtItemHide2);
        h1.setText(shortName);
        h2.setText(sideTitle);

        TextInputEditText t2 = dialogViewEdit.findViewById(R.id.txtItem2); // qty
        TextInputEditText t3 = dialogViewEdit.findViewById(R.id.txtItem3); // buy price
        TextInputEditText t4 = dialogViewEdit.findViewById(R.id.txtItem4); // desc

        btnSave.setOnClickListener(v -> {
            Integer addQty = safeInt(t2);
            Double addPrice = safeDouble(t3);
            String desc = safeText(t4);

            if (addQty == null || addQty <= 0) {
                Toast.makeText(requireContext(), "تعداد خالی است", Toast.LENGTH_SHORT).show();
                return;
            }
            if (addPrice == null || addPrice <= 0) {
                Toast.makeText(requireContext(), "قیمت خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ortalama maliyet hesabı (old mantık) :contentReference[oaicite:4]{index=4}
            float iPrice = (float) (addQty * addPrice);
            float oldPrice = product.getQuantity() * product.getPrice();
            int totalQuantity = addQty + product.getQuantity();

            BigDecimal totalPrice = BigDecimal.valueOf(iPrice + oldPrice);
            BigDecimal newPriceBD = totalPrice.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);
            float newAvgPrice = newPriceBD.floatValue();

            products updateProduct = product;
            updateProduct.setQuantity(totalQuantity);
            updateProduct.setPrice(newAvgPrice);

            boolean successProduct = db.productUpdate(updateProduct);
            if (!successProduct) {
                Toast.makeText(requireContext(), "تکرار کوشش کنید.", Toast.LENGTH_SHORT).show();
                return;
            }

            String now = AppDate.nowDbDateTime(); // yeni DBHelper tarafı bunu kullanıyor :contentReference[oaicite:5]{index=5}
            insertProducts ins = new insertProducts(
                    0,
                    product.getProduct_id(),
                    now,
                    now,
                    addQty,
                    addPrice.floatValue(),
                    desc,
                    true,
                    false
            );

            boolean successInsert = db.addInsertProduct(ins);
            if (successInsert) {
                Toast.makeText(requireContext(), "ثبت گردید !", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dialogEdit.dismiss();
            } else {
                Toast.makeText(requireContext(), "تکرار کوشش کنید.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialogEdit.dismiss());
    }

    private void showSaleDialog(products product, String shortName, String sideTitle) {
        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEdit = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEdit.show();

        Button btnSave = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.product_item_count_sale));
        btnSave.setText(getString(R.string.save));

        TextInputLayout out1 = dialogViewEdit.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogViewEdit.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogViewEdit.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogViewEdit.findViewById(R.id.outItem4);

        out1.setVisibility(View.GONE);
        out2.setHint(getString(R.string.productQuantity));
        out3.setHint(getString(R.string.product_add_price) + " فروش");
        out4.setHint(getString(R.string.productDescription));

        LinearLayout hide = dialogViewEdit.findViewById(R.id.outItemHide1);
        hide.setVisibility(View.VISIBLE);

        TextView h1 = dialogViewEdit.findViewById(R.id.txtItemHide1);
        TextView h2 = dialogViewEdit.findViewById(R.id.txtItemHide2);
        h1.setText(shortName);
        h2.setText(sideTitle);

        TextInputEditText t2 = dialogViewEdit.findViewById(R.id.txtItem2); // qty
        TextInputEditText t3 = dialogViewEdit.findViewById(R.id.txtItem3); // sale price
        TextInputEditText t4 = dialogViewEdit.findViewById(R.id.txtItem4); // desc

        // Stok aşımı engeli (old) :contentReference[oaicite:6]{index=6}
        t2.setOnKeyListener(new View.OnKeyListener() {
            @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    Integer q = safeInt(t2);
                    if (q != null && q > product.getQuantity()) {
                        t2.setText("");
                    }
                }
                return false;
            }
        });

        btnSave.setOnClickListener(v -> {
            Integer saleQty = safeInt(t2);
            Double salePrice = safeDouble(t3);
            String desc = safeText(t4);

            if (saleQty == null || saleQty <= 0) {
                Toast.makeText(requireContext(), "تعداد خالی است", Toast.LENGTH_SHORT).show();
                return;
            }
            if (saleQty > product.getQuantity()) {
                Toast.makeText(requireContext(), "تعداد بیشتر از موجودی است", Toast.LENGTH_SHORT).show();
                return;
            }
            if (salePrice == null || salePrice <= 0) {
                Toast.makeText(requireContext(), "قیمت خالی است", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQuantity = product.getQuantity() - saleQty;

            products updateProduct = product;
            updateProduct.setQuantity(newQuantity);

            boolean successUpdateProduct = db.productUpdate(updateProduct);
            if (!successUpdateProduct) {
                Toast.makeText(requireContext(), "تکرار کوشش کنید.", Toast.LENGTH_SHORT).show();
                return;
            }

            // win hesabı (old) :contentReference[oaicite:7]{index=7}
            float pPrice = (float) ((salePrice - product.getPrice()) * saleQty);
            BigDecimal winBD = BigDecimal.valueOf(pPrice).setScale(2, RoundingMode.HALF_UP);
            float win = winBD.floatValue();

            String now = AppDate.nowDbDateTime();
            sales s = new sales(
                    0,
                    String.valueOf(product.getProduct_id()),
                    salePrice.floatValue(),
                    win,
                    saleQty,
                    now,
                    now,
                    desc,
                    true,
                    false
            );

            boolean successSale = db.addSale(s);
            if (successSale) {
                if (product.getPrice() > salePrice) {
                    float loss = saleQty * (product.getPrice() - salePrice.floatValue());
                    Toast.makeText(requireContext(), loss + " - ضرر کرد", Toast.LENGTH_SHORT).show();
                } else if (product.getPrice() < salePrice) {
                    float profit = saleQty * (salePrice.floatValue() - product.getPrice());
                    Toast.makeText(requireContext(), profit + " - مفاد کرد", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(requireContext(), "فروخته شد", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dialogEdit.dismiss();
            } else {
                Toast.makeText(requireContext(), "تکرار کوشش کنید.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialogEdit.dismiss());
    }

    private void showDeleteDialog(products product, String shortName, String sideTitle) {
        View dialogViewEdit = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null);

        AlertDialog dialogEdit = new AlertDialog.Builder(requireContext())
                .setView(dialogViewEdit)
                .setCancelable(false)
                .create();
        dialogEdit.show();

        Button btnDelete = dialogViewEdit.findViewById(R.id.SaveProductSubmit);
        Button btnCancel = dialogViewEdit.findViewById(R.id.CancelProductSubmit);
        TextView txtTitle = dialogViewEdit.findViewById(R.id.txtTitle);

        txtTitle.setText(getString(R.string.delete) + " جنس");
        btnDelete.setText(getString(R.string.delete));

        LinearLayout hide = dialogViewEdit.findViewById(R.id.outItemHide1);
        hide.setVisibility(View.VISIBLE);

        TextInputLayout out1 = dialogViewEdit.findViewById(R.id.outItem1);
        TextInputLayout out2 = dialogViewEdit.findViewById(R.id.outItem2);
        TextInputLayout out3 = dialogViewEdit.findViewById(R.id.outItem3);
        TextInputLayout out4 = dialogViewEdit.findViewById(R.id.outItem4);

        out1.setVisibility(View.GONE);
        out2.setVisibility(View.GONE);
        out3.setVisibility(View.GONE);
        out4.setVisibility(View.GONE);

        TextView h1 = dialogViewEdit.findViewById(R.id.txtItemHide1);
        TextView h2 = dialogViewEdit.findViewById(R.id.txtItemHide2);
        h1.setText(shortName);
        h2.setText(sideTitle);

        btnDelete.setOnClickListener(v -> {
            products updateProduct = product;
            updateProduct.setDeleteStatus(true);

            boolean success = db.productUpdate(updateProduct);
            if (success) {
                Toast.makeText(requireContext(), "حذف شد", Toast.LENGTH_SHORT).show();
                onFragmentUpdated();
                dialogEdit.dismiss();
            } else {
                Toast.makeText(requireContext(), "حذف نشد", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialogEdit.dismiss());
    }

    // PDF HTML (senin verdiğin sürüm)
    private String buildProductsPdfHtml(String title, List<products> data, @Nullable String query) {
        int rowNo = 0;
        int totalQty = 0;
        double capital = 0.0;

        if (data != null) {
            for (products p : data) {
                if (p == null) continue;
                if (p.isDeleteStatus()) continue;
                totalQty += p.getQuantity();
                capital += (p.getPrice() * p.getQuantity());
            }
        }

        String q = (query == null) ? "" : query.trim();

        StringBuilder sb = new StringBuilder();
        sb.append("<html dir='rtl' lang='fa'><head><meta charset='utf-8'/>")
                .append("<style>")
                .append("body{font-family:sans-serif;padding:16px;direction:rtl;}")
                .append("h2{margin:0 0 8px 0;font-size:18px;}")
                .append(".meta{color:#555;margin:8px 0 12px 0;font-size:12px;line-height:1.6;}")
                .append(".badge{display:inline-block;padding:2px 8px;border:1px solid #ddd;border-radius:999px;background:#fafafa;color:#333;font-size:11px;margin-left:6px;}")
                .append(".summary{border:1px solid #e6e6e6;background:#fcfcfc;border-radius:10px;padding:10px 12px;margin:10px 0 14px 0;}")
                .append(".row{display:flex;justify-content:space-between;gap:12px;padding:4px 0;}")
                .append(".k{color:#444;font-weight:600;}")
                .append(".v{color:#111;font-weight:700;}")
                .append("table{width:100%;border-collapse:collapse;font-size:12px;}")
                .append("th,td{border:1px solid #ddd;padding:8px;text-align:right;vertical-align:top;}")
                .append("th{background:#f3f3f3;font-weight:700;}")
                .append("th:first-child, td:first-child{width:30px;text-align:center;}")
                .append("</style></head><body>");

        sb.append("<h2>").append(esc(title)).append("</h2>");

        sb.append("<div class='meta'>")
                .append("<span class='badge'>گزارش</span>")
                .append("<span class='badge'>محصولات</span>")
                .append("<br/>")
                .append("جستجو: ").append(esc(q))
                .append("</div>");

        sb.append("<div class='summary'>")
                .append("<div class='row'><span class='k'>تعداد اقلام:</span><span class='v'>")
                .append(data == null ? 0 : data.size()).append("</span></div>")
                .append("<div class='row'><span class='k'>مجموع تعداد:</span><span class='v'>")
                .append(totalQty).append("</span></div>")
                .append("<div class='row'><span class='k'>سرمایه:</span><span class='v'>")
                .append(formatMoney(capital)).append("</span></div>")
                .append("</div>");

        sb.append("<table><thead><tr>")
                .append("<th>#</th>")
                .append("<th>نام</th>")
                .append("<th>تعداد</th>")
                .append("<th>قیمت خرید</th>")
                .append("<th>مبلغ</th>")
                .append("</tr></thead><tbody>");

        if (data != null) {
            for (products p : data) {
                if (p == null) continue;
                if (p.isDeleteStatus()) continue;

                int qty = p.getQuantity();
                if (qty <= 0) continue;   // stok 0 ise PDF'e girmesin

                rowNo++;

                double price = p.getPrice();
                double amount = qty * price;

                sb.append("<tr>")
                        .append("<td>").append(rowNo).append("</td>")
                        .append("<td>").append(esc(nullToEmpty(p.getName()))).append("</td>")
                        .append("<td>").append(qty).append("</td>")
                        .append("<td>").append(formatMoney(price)).append("</td>")
                        .append("<td>").append(formatMoney(amount)).append("</td>")
                        .append("</tr>");
            }
        }

        sb.append("</tbody></table></body></html>");
        return sb.toString();
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

    private static Integer safeInt(@Nullable TextInputEditText et) {
        try {
            String s = safeText(et);
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Double safeDouble(@Nullable TextInputEditText et) {
        try {
            String s = safeText(et);
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception ignore) {
            return null;
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

    private String formatMoney(double v) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        df.applyPattern("#,##0.00");
        return df.format(v);
    }
}

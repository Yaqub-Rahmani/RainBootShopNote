package com.example.rainbootshopnote.myAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rainbootshopnote.Dto.ProductDTO;
import com.example.rainbootshopnote.R;
import com.example.rainbootshopnote.utils.date.AppDate;
import com.example.rainbootshopnote.utils.date.DateNormalize;

import java.util.List;

public class ProductAdapter extends BaseAdapter {

    private final Context context;
    private final List<ProductDTO> productList;

    private boolean showSolar = false;

    public ProductAdapter(Context context, List<ProductDTO> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int i) {
        return productList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setShowSolar(boolean v) {
        this.showSolar = v;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        }

        TextView name = view.findViewById(R.id.txt_item_info_name);
        TextView price = view.findViewById(R.id.txt_item_info_price);
        TextView quantity = view.findViewById(R.id.txt_item_info_count);
        TextView createDate = view.findViewById(R.id.txt_item_info_date);
        TextView win = view.findViewById(R.id.txt_item_info_win);
        LinearLayout iLayout = view.findViewById(R.id.layout_item_info);
        TextView infoName = view.findViewById(R.id.txt_item_info);

        ProductDTO product = productList.get(position);

        boolean isSale = product.isSaleOrInsert();
        String typeFa = isSale ? "فروش" : "خرید";

        // Basit row background (istersen CashAdapter gibi drawable ile de yapabiliriz)
        iLayout.setBackgroundResource(R.drawable.bg_row_soft_white);

        // Type tag
        infoName.setText(typeFa);
        infoName.setTextColor(Color.WHITE);
        infoName.setBackgroundResource(isSale ? R.drawable.bg_tag_blue : R.drawable.bg_tag_red);

        name.setText(product.getName() == null ? "" : product.getName());
        price.setText(String.valueOf(product.getPrice()) + " - " + typeFa);
        quantity.setText(String.valueOf(product.getQuantity()) + " - تعداد");

        // Win sadece satışta göster
        if (isSale) {
            win.setVisibility(View.VISIBLE);

            if (product.getWin() < 0) {
                win.setTextColor(Color.RED);
                win.setText(String.valueOf(product.getWin()) + " - ضرر");
            } else {
                win.setTextColor(Color.BLACK);
                win.setText(String.valueOf(product.getWin()) + " - مفاد");
            }
        } else {
            win.setVisibility(View.GONE);
        }

        // Date: sadece gün + solar/gregorian
        String isoDay = DateNormalize.extractIsoDay(product.getCreateDate());
        String uiDate = AppDate.toUiDate(isoDay, showSolar);
        createDate.setText(uiDate == null ? "" : uiDate);

        return view;
    }

    // Listeyi güncelle
    public void updateList(List<ProductDTO> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }
}

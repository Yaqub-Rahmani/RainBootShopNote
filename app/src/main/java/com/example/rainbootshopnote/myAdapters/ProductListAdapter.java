package com.example.rainbootshopnote.myAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rainbootshopnote.R;
import com.example.rainbootshopnote.entities.products;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends BaseAdapter {

    private final Context context;
    private final List<products> productList = new ArrayList<>();

    public ProductListAdapter(Context context, List<products> initial) {
        this.context = context;
        if (initial != null) productList.addAll(initial);
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        products p = productList.get(position);
        return p == null ? position : p.getProduct_id();
    }


    public interface OnProductClickListener {
        void onProductClick(products p);
    }

    private OnProductClickListener listener;

    public void setOnProductClickListener(OnProductClickListener l) {
        this.listener = l;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_products, parent, false);
        }

        TextView name = v.findViewById(R.id.txt_product_item_name);
        TextView qty  = v.findViewById(R.id.txt_product_item_count);
        TextView price = v.findViewById(R.id.txt_product_item_price);

        View nameArea = v.findViewById(R.id.product_name_area); // NEW

        products p = productList.get(position);

        String n = (p == null || p.getName() == null) ? "" : p.getName();
        int q = (p == null) ? 0 : p.getQuantity();
        float pr = (p == null) ? 0f : p.getPrice();

        name.setText(n);
        qty.setText(q + " - تعداد موجود");
        price.setText(pr + " - قیمت خرید");

        // Click only on name area (or set for entire row)
        nameArea.setOnClickListener(view -> {
            if (listener != null && p != null) listener.onProductClick(p);
        });

        return v;
    }

    public void updateList(List<products> newList) {
        productList.clear();
        if (newList != null) productList.addAll(newList);
        notifyDataSetChanged();
    }
}

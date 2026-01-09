package com.example.rainbootshopnote.myAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rainbootshopnote.Dto.CashDTO;
import com.example.rainbootshopnote.Dto.ProductDTO;
import com.example.rainbootshopnote.R;
import com.example.rainbootshopnote.utils.date.AppDate;
import com.example.rainbootshopnote.utils.date.DateNormalize;

import java.util.List;

public class CashAdapter extends BaseAdapter {

    private Context context;
    private List<CashDTO> listCashDTOS;

    public CashAdapter(Context context, List<CashDTO> listCashDTOS) {
        this.context = context;
        this.listCashDTOS = listCashDTOS;
    }

    @Override
    public int getCount() {
        return listCashDTOS.size();
    }

    @Override
    public Object getItem(int i) {
        return listCashDTOS.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
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

        CashDTO cashDTO = listCashDTOS.get(position);

        // Row background
//        iLayout.setBackgroundColor(cashDTO.isStatus() ? Color.LTGRAY : Color.WHITE);

        if (cashDTO.isStatus()) {
            // hesaplaşmış → yumuşak gri
            iLayout.setBackgroundResource(R.drawable.bg_row_soft_gray);
        } else {
            // aktif → beyaz
            iLayout.setBackgroundResource(R.drawable.bg_row_soft_white);
        }

        // Type label
        boolean isDeposit = cashDTO.isDepositOrWithdrawal();
        String typeFa = isDeposit ? "رسید" : "باقی";

        // Type tag
        infoName.setText(typeFa);
        infoName.setTextColor(Color.WHITE);
        infoName.setBackgroundResource(isDeposit ? R.drawable.bg_tag_blue : R.drawable.bg_tag_red);


        // Diğer alanlar
        String statusText = cashDTO.isStatus() ? "" : "حسابی شد";

        name.setText(cashDTO.getName() == null ? "" : cashDTO.getName());
        price.setText(cashDTO.getPayUser() == null ? "" : String.valueOf(cashDTO.getPayUser()));
        quantity.setText(String.valueOf(cashDTO.getPrice()) + " - " + typeFa);
        win.setText(statusText);

        // ✅ Liste: sadece tarih (saat yok)
        String isoDay = DateNormalize.extractIsoDay(cashDTO.getCreateDate());
        String uiDate = AppDate.toUiDate(isoDay, showSolar);
        createDate.setText(uiDate == null ? "" : uiDate);

        return view;
    }

    // Yeni bir metot ekleyerek listeyi güncelle
    public void updateList(List<CashDTO> newList) {
        listCashDTOS.clear();
        listCashDTOS.addAll(newList);
        notifyDataSetChanged(); // Adaptörü güncelle
    }


    private boolean showSolar = false;
    public void setShowSolar(boolean v) { showSolar = v; notifyDataSetChanged(); }


}

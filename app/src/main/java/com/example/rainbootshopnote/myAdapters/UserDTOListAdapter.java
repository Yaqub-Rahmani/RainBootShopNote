package com.example.rainbootshopnote.myAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rainbootshopnote.Dto.UserDTO;
import com.example.rainbootshopnote.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UserDTOListAdapter extends BaseAdapter {

    private final Context context;
    private final List<UserDTO> list = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    public UserDTOListAdapter(Context context, List<UserDTO> initial) {
        this.context = context;
        if (initial != null) list.addAll(initial);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        UserDTO u = list.get(position);
        return u == null ? position : u.getUser_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_user_dto, parent, false);
        }

        TextView name = v.findViewById(R.id.txt_user_item_name);
        TextView mobile = v.findViewById(R.id.txt_user_item_mobile);
//        TextView dep = v.findViewById(R.id.txt_user_item_deposit_amount);
//        TextView wit = v.findViewById(R.id.txt_user_item_withdrawal_amount);
        TextView diffView = v.findViewById(R.id.txt_item_status_info);

        UserDTO u = list.get(position);

        String n = (u == null || u.getName() == null) ? "" : u.getName();
        String m = (u == null || u.getMobile() == null) ? "" : u.getMobile();

        double depAmount = (u == null) ? 0.0 : u.getDepositAmount();
        double witAmount = (u == null) ? 0.0 : u.getWithdrawalsAmount();
        double diff = depAmount - witAmount;

        name.setText(n);
        mobile.setText(m);

//        dep.setText(df.format(depAmount) + " - رسید");
//        wit.setText(df.format(witAmount) + " - باقی");

        diffView.setText(df.format(diff));
        diffView.setBackgroundColor(Color.WHITE);
        diffView.setTextColor(diff < 0 ? Color.RED : Color.BLACK);

        return v;
    }

    public void updateList(List<UserDTO> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }
}

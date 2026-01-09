package com.example.rainbootshopnote.myAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rainbootshopnote.R;
import com.example.rainbootshopnote.entities.admins;

import java.util.List;

public class AdminAdapter extends BaseAdapter {

    private Context context;
    private List<admins> _admins;

    public AdminAdapter(Context context, List<admins> _admins) {
        this.context = context;
        this._admins = _admins;
    }

    @Override
    public int getCount() {
        return _admins.size();
    }

    @Override
    public Object getItem(int position) {
        return _admins.get(position);
    }

    @Override
    public long getItemId(int position) {
        return _admins.get(position).getAdmin_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
/*        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_admin, parent, false);
        }

        admins admin = _admins.get(position);

        //TextView IDTextView = convertView.findViewById(R.id.adminID);
        TextView nameTextView = convertView.findViewById(R.id.adminName);
        TextView userNameTextView = convertView.findViewById(R.id.adminUserName);*/

        //IDTextView.setText(admin.getAdmin_id());
//        nameTextView.setText(admin.getName());
//        userNameTextView.setText(admin.getUser_name());

        return convertView;
    }
}

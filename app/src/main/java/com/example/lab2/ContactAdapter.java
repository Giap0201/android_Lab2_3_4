package com.example.lab2;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private Context context;
    private List<Contact> dsContact;
    private LayoutInflater inflater;

    public ContactAdapter(Context context, List<Contact> dsContact) {
        this.context = context;
        this.dsContact = dsContact;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return dsContact.size();
    }

    @Override
    public Object getItem(int position) {
        return dsContact.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_contact, null);
        }
        TextView tvName = convertView.findViewById(R.id.tvHoTen);
        TextView tvPhone = convertView.findViewById(R.id.tvSoDienThoai);
        ImageView imageView = convertView.findViewById(R.id.imAvatar);
        CheckBox cbCheck = convertView.findViewById(R.id.cbCheck);

        Contact contact = dsContact.get(position);
        tvName.setText(contact.getName());
        tvPhone.setText(contact.getPhone());

        // Xu li anh
        if(contact.getImagePath() != null && !contact.getImagePath().isEmpty()){
            imageView.setImageURI(Uri.parse(contact.getImagePath()));
        }else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }

        // xu li check box (Tranh loi nhay checkbox khi cuon)
        cbCheck.setOnCheckedChangeListener(null);
        cbCheck.setChecked(contact.isStatus());
        cbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                contact.setStatus(isChecked);
            }
        });
        return convertView;
    }
}

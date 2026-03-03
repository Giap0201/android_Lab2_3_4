package com.example.lab2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    List<Contact> dsContact;
    ContactAdapter adapter;
    CheckBox cbCheck;
    Button btnThemMoi, btnXoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addControls();
        addEvents();
    }

    private void addEvents() {
        btnThemMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddContact.class);
                startActivityForResult(intent, 100);
            }
        });
        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDeleteDialog();
            }
        });
    }

    private void displayDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn có chắc chắn xoá các mục đã chọn?");
        builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleXoa();
            }
        });
        builder.setNegativeButton("Huỷ", null);
        builder.show();
    }

    private void handleXoa() {
        List<Contact> dsDelete = new ArrayList<>();
        for (Contact c : dsContact) {
            if (c.isStatus()) {
                dsDelete.add(c);
            }
        }
        if (dsDelete.isEmpty()) {
            Toast.makeText(this, "Chưa chọn liên hệ cần xoá", Toast.LENGTH_SHORT).show();
            return;
        }
        dsContact.removeAll(dsDelete);
        adapter.notifyDataSetChanged();
        ;
        Toast.makeText(this, "Đã xoá " + dsDelete.size() + " liên hệ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int id = bundle.getInt("id");
                String hoTen = bundle.getString("hoVaTen");
                String soDienThoai = bundle.getString("soDienThoai");
                String uriAnh = bundle.getString("uriAnh");
                dsContact.add(new Contact(id, hoTen, soDienThoai, false, uriAnh));
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void addControls() {
        listView = findViewById(R.id.lvContact);
        btnXoa = findViewById(R.id.btnXoa);
        btnThemMoi = findViewById(R.id.btnThemMoi);
        cbCheck = findViewById(R.id.cbCheck);
        dsContact = new ArrayList<>();
        dsContact.add(new Contact(1, "Nguyen Van A", "0987654321", false, null));
        dsContact.add(new Contact(2, "Nguyen Van B", "0987654322", false, null));
        dsContact.add(new Contact(3, "Nguyen Van C", "0987654323", false, null));
        dsContact.add(new Contact(4, "Nguyen Van D", "0987654324", false, null));
        dsContact.add(new Contact(5, "Nguyen Van E", "0987654325", false, null));
        adapter = new ContactAdapter(this, dsContact);
        listView.setAdapter(adapter);
    }
}
package com.example.lab2;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
    int selectedItem;

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
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = position;
                return false;
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull android.view.MenuItem item) {
        Contact c = dsContact.get(selectedItem);
        int id = item.getItemId();
        if(id == R.id.ctxEdit){
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", c.getId());
            bundle.putString("hoVaTen", c.getName());
            bundle.putString("soDienThoai", c.getPhone());
            bundle.putString("uriAnh", c.getImagePath());
            intent.putExtras(bundle);
            startActivityForResult(intent, 200);
            return true;
        }
        else if (id == R.id.ctxCall) {
            // 5. GỌI ĐIỆN THOẠI (Dùng ACTION_DIAL để an toàn, mở bàn phím số)
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + c.getPhone()));
            startActivity(intent);
            return true;

        } else if (id == R.id.ctxSms) {
            // 6. NHẮN TIN SMS
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + c.getPhone()));
            startActivity(intent);
            return true;

        } else if (id == R.id.ctxShare) {
            // 7. CHIA SẺ (Lên FB, Zalo, Copy text...)
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            // Nội dung sẽ được gửi đi:
            String noiDung = "Số điện thoại của " + c.getName() + " là: " + c.getPhone();
            intent.putExtra(Intent.EXTRA_TEXT, noiDung);
            startActivity(Intent.createChooser(intent, "Chia sẻ liên hệ qua..."));
            return true;

        } else if (id == R.id.ctxCamera) {
            // 8. CHỤP ẢNH
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            // Mình dùng mã 300 để phân biệt với 100 (Thêm) và 200 (Sửa) nhé
            startActivityForResult(intent, 300);
            return true;
        } else if (id == R.id.ctxDelete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận");
            builder.setMessage("Bạn có chắc chắn xoá các mục đã chọn?");
            builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dsContact.remove(c);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Đã xoá 1 liên hệ", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Huỷ", null);
            builder.show();
        }
        return super.onContextItemSelected(item);
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
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int id = bundle.getInt("id");
                String hoTen = bundle.getString("hoVaTen");
                String soDienThoai = bundle.getString("soDienThoai");
                String uriAnh = bundle.getString("uriAnh");
                Contact c = dsContact.get(selectedItem);
                c.setId(id);
                c.setName(hoTen);
                c.setPhone(soDienThoai);
                c.setImagePath(uriAnh);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            }
        }
        // XỬ LÝ KHI CHỤP ẢNH XONG (MÃ 300)
        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            // Lấy tấm ảnh thu nhỏ từ Camera
            android.graphics.Bitmap bitmap = (android.graphics.Bitmap) data.getExtras().get("data");

            if (bitmap != null) {
                // Gọi hàm lưu ảnh ra file
                Uri uriAnhMoi = saveBitmapToCache(bitmap);

                if (uriAnhMoi != null) {
                    // SỬ DỤNG BIẾN selectedItem CỦA BẠN ĐỂ CẬP NHẬT ĐÚNG NGƯỜI
                    Contact c = dsContact.get(selectedItem);
                    c.setImagePath(uriAnhMoi.toString());

                    // Vẽ lại giao diện
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Chụp ảnh thành công!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.context_menu, menu);
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
        registerForContextMenu(listView);
    }

    // Hàm này giúp chuyển đổi ảnh Bitmap từ Camera thành 1 file lưu trong máy
    private Uri saveBitmapToCache(android.graphics.Bitmap bitmap) {
        try {
            java.io.File file = new java.io.File(getCacheDir(), "avatar_camera_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
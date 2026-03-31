package com.example.lab2;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    // private MyDB db; // COMMENT LẠI DB
    private ContentProvider cp;

    // Khai báo mã request xin quyền
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

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

    private void addControls() {
        listView = findViewById(R.id.lvContact);
        btnXoa = findViewById(R.id.btnXoa);
        btnThemMoi = findViewById(R.id.btnThemMoi);
        cbCheck = findViewById(R.id.cbCheck);

        // --- COMMENT LẠI TOÀN BỘ PHẦN SQLITE ---
        /*
        db = new MyDB(this, "Contact.db", null, 1);
        dsContact = db.getAllContacts();
        if (dsContact.size() == 0) {
            db.addContact(new Contact(1, "Nguyen Van A", "0987654321", false, null));
            ...
            dsContact = db.getAllContacts();
        }
        adapter = new ContactAdapter(this, dsContact);
        listView.setAdapter(adapter);
        */

        // DÙNG CONTENT PROVIDER ĐỂ HIỂN THỊ DANH BẠ
        dsContact = new ArrayList<>();
        adapter = new ContactAdapter(this, dsContact);
        listView.setAdapter(adapter);

        ShowContact(); // Gọi hàm xin quyền và load danh bạ

        registerForContextMenu(listView);
    }

    private void ShowContact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Kiểm tra xem đã có đủ cả 2 quyền chưa
            boolean hasRead = checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
            boolean hasWrite = checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;

            if (!hasRead || !hasWrite) {
                // Xin cả 2 quyền cùng lúc
                requestPermissions(new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS
                }, PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                loadContactsFromDevice();
            }
        } else {
            loadContactsFromDevice();
        }
    }

    // Xử lý kết quả sau khi người dùng bấm Cho Phép / Từ chối quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Đã cấp quyền -> Load dữ liệu
                loadContactsFromDevice();
            } else {
                Toast.makeText(this, "Cần cấp quyền để đọc danh bạ!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hàm tách riêng để load dữ liệu
    private void loadContactsFromDevice() {
        cp = new ContentProvider(this);
        dsContact.clear();
        dsContact.addAll(cp.getAllContact());
        adapter.notifyDataSetChanged();
    }


    private void addContactToSystem(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // 1. Tạo một RawContact mới (Trống)
        int rawContactInsertIndex = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // 2. Nhét Tên vào RawContact vừa tạo
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // 3. Nhét Số điện thoại vào
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        // 4. Thực thi lệnh lưu
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu vào danh bạ máy!", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateContactInSystem(int contactId, String newName, String newPhone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String contactIdStr = String.valueOf(contactId);

        // Lệnh 1: Cập nhật Tên
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{contactIdStr, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                .build());

        // Lệnh 2: Cập nhật Số điện thoại
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{contactIdStr, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newPhone)
                .build());

        try {
            // Gửi lệnh cho hệ thống thực thi
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi cập nhật danh bạ máy!", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteContactFromSystem(int contactId) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        String[] args = new String[] { String.valueOf(contactId) };

        // Tạo lệnh xoá liên hệ dựa trên ID
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + " = ?", args)
                .build());

        try {
            // Thực thi lệnh xoá
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xoá khỏi danh bạ máy!", Toast.LENGTH_SHORT).show();
        }
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
            bundle.putBoolean("status", c.isStatus());
            intent.putExtras(bundle);
            startActivityForResult(intent, 200);
            return true;
        }
        else if (id == R.id.ctxCall) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + c.getPhone()));
            startActivity(intent);
            return true;

        } else if (id == R.id.ctxSms) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + c.getPhone()));
            startActivity(intent);
            return true;

        } else if (id == R.id.ctxShare) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String noiDung = "Số điện thoại của " + c.getName() + " là: " + c.getPhone();
            intent.putExtra(Intent.EXTRA_TEXT, noiDung);
            startActivity(Intent.createChooser(intent, "Chia sẻ liên hệ qua..."));
            return true;


        } else if (id == R.id.ctxCamera) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 300);
            return true;

        } else if (id == R.id.ctxEmail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            String email = "test_email@gmail.com";
            intent.setData(Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Thư gửi từ App Danh Bạ tới " + c.getName());
            startActivity(intent);
            return true;

        } else if (id == R.id.ctxUrl) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://www.google.com";
            intent.setData(Uri.parse(url));
            startActivity(intent);
            return true;

//        } else if (id == R.id.ctxDelete){
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("Xác nhận");
//            builder.setMessage("Bạn có chắc chắn xoá liên hệ này?");
//            builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // db.deleteContact(c.getId()); // COMMENT LẠI DB
//                    dsContact.remove(c);
//                    adapter.notifyDataSetChanged();
//                    Toast.makeText(MainActivity.this, "Đã xoá 1 liên hệ", Toast.LENGTH_SHORT).show();
//                }
//            });
//            builder.setNegativeButton("Huỷ", null);
//            builder.show();
//        }
        } else if (id == R.id.ctxDelete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Xác nhận");
            builder.setMessage("Bạn có chắc chắn xoá liên hệ này?");
            builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // 1. Xoá thẳng khỏi danh bạ gốc máy
                    deleteContactFromSystem(c.getId());

                    // 2. Load lại danh sách lên màn hình
                    loadContactsFromDevice();

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
                // db.deleteContact(c.getId()); // COMMENT LẠI DB
            }
        }
        if (dsDelete.isEmpty()) {
            Toast.makeText(this, "Chưa chọn liên hệ cần xoá", Toast.LENGTH_SHORT).show();
            return;
        }
        dsContact.removeAll(dsDelete);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Đã xoá " + dsDelete.size() + " liên hệ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == RESULT_OK) {
//            Bundle bundle = data.getExtras();
//            if (bundle != null) {
//                int id = bundle.getInt("id");
//                String hoTen = bundle.getString("hoVaTen");
//                String soDienThoai = bundle.getString("soDienThoai");
//                String uriAnh = bundle.getString("uriAnh");
//
//                Contact newContact = new Contact(id, hoTen, soDienThoai, false, uriAnh);
//
//                // db.addContact(newContact); // COMMENT LẠI DB
//                dsContact.add(newContact);
//                adapter.notifyDataSetChanged();
//                Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
//            }
//        }

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String hoTen = bundle.getString("hoVaTen");
                String soDienThoai = bundle.getString("soDienThoai");

                // 1. Lưu thẳng vào danh bạ gốc của điện thoại
                addContactToSystem(hoTen, soDienThoai);

                // 2. Đọc lại danh bạ để cập nhật danh sách trên màn hình
                loadContactsFromDevice();

                Toast.makeText(this, "Thêm vào danh bạ thành công!", Toast.LENGTH_SHORT).show();
            }
        }

//        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
//            Bundle bundle = data.getExtras();
//            if (bundle != null) {
//                Contact c = dsContact.get(selectedItem);
//                c.setId(bundle.getInt("id"));
//                c.setName(bundle.getString("hoVaTen"));
//                c.setPhone(bundle.getString("soDienThoai"));
//                c.setImagePath(bundle.getString("uriAnh"));
//                c.setStatus(bundle.getBoolean("status"));
//
//                // db.updateContact(c); // COMMENT LẠI DB
//                adapter.notifyDataSetChanged();
//                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
//            }
//        }
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Contact c = dsContact.get(selectedItem);
                String tenMoi = bundle.getString("hoVaTen");
                String soMoi = bundle.getString("soDienThoai");

                // 1. Cập nhật thẳng vào danh bạ gốc của điện thoại
                updateContactInSystem(c.getId(), tenMoi, soMoi);

                // 2. Load lại danh sách lên màn hình
                loadContactsFromDevice();

                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            android.graphics.Bitmap bitmap = (android.graphics.Bitmap) data.getExtras().get("data");

            if (bitmap != null) {
                Uri uriAnhMoi = saveBitmapToCache(bitmap);

                if (uriAnhMoi != null) {
                    Contact c = dsContact.get(selectedItem);
                    c.setImagePath(uriAnhMoi.toString());

                    // db.updateContact(c); // COMMENT LẠI DB
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
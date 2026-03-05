package com.example.lab2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditActivity extends AppCompatActivity {

    EditText etId, etHoTen, etPhone;
    Button btnLuu, btnHuy;
    ImageView imgAvatar;
    String uriAnhHienTai = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addControls();
        nhanDuLieuTuMain();
        addEvents();
    }

    private void nhanDuLieuTuMain() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            int id = bundle.getInt("id");
            String ten = bundle.getString("hoVaTen");
            String sdt = bundle.getString("soDienThoai");
            uriAnhHienTai = bundle.getString("uriAnh");
            etId.setText(String.valueOf(id));
            etHoTen.setText(ten);
            etPhone.setText(sdt);

            if (uriAnhHienTai != null && !uriAnhHienTai.isEmpty()) {
                try {
                    Uri uri = Uri.parse(uriAnhHienTai);
                    imgAvatar.setImageURI(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addEvents() {
        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnLuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSaveData();
            }
        });
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

    }

    private void handleSaveData() {
        String tenMoi = etHoTen.getText().toString();
        String sdtMoi = etPhone.getText().toString();

        if (tenMoi.isEmpty() || sdtMoi.isEmpty()) {
            Toast.makeText(this, "Không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("id", Integer.parseInt(etId.getText().toString()));
        bundle.putString("hoVaTen", tenMoi);
        bundle.putString("soDienThoai", sdtMoi);
        bundle.putString("uriAnh", uriAnhHienTai);

        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void chooseImage() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(i, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                imgAvatar.setImageURI(uri);
                uriAnhHienTai = uri.toString();
            }
        }
    }

    private void addControls() {
        etId = findViewById(R.id.etIdEdit);
        etHoTen = findViewById(R.id.etHoVaTenEdit);
        etPhone = findViewById(R.id.etSoDienThoaiEdit);
        btnLuu = findViewById(R.id.btnLuuEdit);
        btnHuy = findViewById(R.id.btnHuyEdit);
        imgAvatar = findViewById(R.id.imgAvatarEdit);
    }
}
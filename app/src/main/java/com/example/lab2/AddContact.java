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

public class AddContact extends AppCompatActivity {
    EditText etId, etHoVaTen, etSoDienThoai;
    Button btnLuu, btnHuy;
    ImageView imageView;
    String uriAnh = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addControls();
        addEvents();
    }

    private void addEvents() {
        btnHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.setType("image/*");
//                chonAnhLauncher.launch(intent);
                chooseImage();
            }
        });
        btnLuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSave();
            }
        });
    }

//    ActivityResultLauncher<Intent> chonAnhLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                    Uri uri = result.getData().getData();
//                    imageView.setImageURI(uri);
//                    uriAnh = uri.toString();
//                }
//            }
//    );

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
                imageView.setImageURI(uri);
                uriAnh = uri.toString();
            }
        }
    }

    private void handleSave() {
        String id = etId.getText().toString();
        String hoVaTen = etHoVaTen.getText().toString();
        String soDienThoai = etSoDienThoai.getText().toString();
        if (id.isEmpty() || hoVaTen.isEmpty() || soDienThoai.isEmpty()) {
            Toast.makeText(AddContact.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("id", Integer.parseInt(id));
        bundle.putString("hoVaTen", hoVaTen);
        bundle.putString("soDienThoai", soDienThoai);
        bundle.putString("uriAnh", uriAnh);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void addControls() {
        etId = findViewById(R.id.etId);
        etHoVaTen = findViewById(R.id.etHoVaTen);
        etSoDienThoai = findViewById(R.id.etSoDienThoai);
        btnLuu = findViewById(R.id.btnLuu);
        btnHuy = findViewById(R.id.btnHuy);
        imageView = findViewById(R.id.imageView);
    }
}
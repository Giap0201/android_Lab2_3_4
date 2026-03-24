package com.example.lab2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyDB extends SQLiteOpenHelper {

    public static final String TableName = "ContactTable";
    public static final String Id = "Id";
    public static final String Name = "FullName";
    public static final String Phone = "PhoneNumber";
    public static final String Image = "Image";

    public MyDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "create table " + TableName + " ("
                + Id + " integer primary key, "
                + Name + " text, "
                + Phone + " text, "
                + Image + " text)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable = "drop table if exists " + TableName;
        db.execSQL(dropTable);
        onCreate(db);
    }

    // ==========================================================
    // CÁC HÀM HỖ TRỢ THAO TÁC DỮ LIỆU (CRUD)
    // ==========================================================

    // 1. Lấy toàn bộ danh sách liên hệ
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "select * from " + TableName;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(0));
                contact.setName(cursor.getString(1));
                contact.setPhone(cursor.getString(2));
                contact.setStatus(false); // Trạng thái checkbox mặc định khi load lên
                contact.setImagePath(cursor.getString(3));

                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }

    // 2. Thêm một liên hệ mới
    public void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Id, contact.getId());
        values.put(Name, contact.getName());
        values.put(Phone, contact.getPhone());
        values.put(Image, contact.getImagePath());

        db.insert(TableName, null, values);
        db.close();
    }

    // 3. Cập nhật liên hệ (khi Edit)
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Name, contact.getName());
        values.put(Phone, contact.getPhone());
        values.put(Image, contact.getImagePath());

        // Cập nhật dựa trên Id
        return db.update(TableName, values, Id + " = ?",
                new String[]{String.valueOf(contact.getId())});
    }

    // 4. Xóa một liên hệ
    public void deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TableName, Id + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}
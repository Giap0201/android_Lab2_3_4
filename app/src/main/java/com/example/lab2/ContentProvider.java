package com.example.lab2;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContentProvider {
    private Activity activity;

    public ContentProvider(Activity activity) {
        this.activity = activity;
    }

    public ArrayList<Contact> getAllContact() {
        ArrayList<Contact> listContact = new ArrayList<>();
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        };

        Cursor cursor = activity.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Truyền tham số đúng thứ tự: id, name, phone, status, imagePath
                    Contact c = new Contact(
                            cursor.getInt(0),     // ID
                            cursor.getString(1),  // Name
                            cursor.getString(2),  // Phone
                            false,                // Status mặc định
                            cursor.getString(3)   // Image
                    );
                    listContact.add(c);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return listContact;
    }
}
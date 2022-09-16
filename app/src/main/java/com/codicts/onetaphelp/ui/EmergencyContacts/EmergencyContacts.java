package com.codicts.onetaphelp.ui.EmergencyContacts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.codicts.onetaphelp.Adapters.EmergencyContactsAdapter;
import com.codicts.onetaphelp.Models.EmergencyContact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;

import com.codicts.onetaphelp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EmergencyContacts extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private RecyclerView contactsList;
    private RecyclerView.Adapter contactsListAdapter;
    private RecyclerView.LayoutManager listLayoutManager;

    String contactListString;
    JSONArray contactsData = new JSONArray();
    private ArrayList<EmergencyContact> contactsArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FloatingActionButton fab = findViewById(R.id.fab_addContacts);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (contactsData.length() < 5) {
                    Uri uri = Uri.parse("content://contactsJSON");

                    Intent contactPicker = new Intent(Intent.ACTION_PICK, uri);
                    contactPicker.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(contactPicker, REQUEST_CODE);

                } else {
                    Snackbar.make(view, "5 Contacts already added", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        preferences = getSharedPreferences("com.codicts.onetaphelp", MODE_PRIVATE);

        contactListString = preferences.getString("contacts", null);

        if (contactListString != null) {
            try {
                JSONObject contactListJSON = new JSONObject(contactListString);
                contactsData = contactListJSON.getJSONArray("contacts");
                for (int i=0;i<contactsData.length(); i++){
                    EmergencyContact contact = new EmergencyContact();
                    contact.contactName = contactsData.getJSONObject(i).getString("contactName");
                    contact.contactPhno = contactsData.getJSONObject(i).getString("contactPhNo");
                    contactsArrayList.add(contact);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        contactsList = findViewById(R.id.emergencyContactsList);
        listLayoutManager = new LinearLayoutManager(this);
        contactsList.setHasFixedSize(true);
        contactsList.setLayoutManager(listLayoutManager);
        contactsListAdapter = new EmergencyContactsAdapter(contactsArrayList, this);
        contactsList.setAdapter(contactsListAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberColumnIndex);

                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(nameColumnIndex);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (contactsArrayList.stream().filter(p -> p.getContactPhno().equals(number)).count() == 0) {
                        EmergencyContact contact = new EmergencyContact();
                        contact.contactPhno = number;
                        contact.contactName = name;

                        JSONObject contacts = new JSONObject();
                        JSONObject contactJSON = new JSONObject();

                        contactsArrayList.add(contact);
                        contactsListAdapter.notifyDataSetChanged();
                        try {
                            contactJSON.put("contactName", name);
                            contactJSON.put("contactPhNo", number);
                            contactsData.put(contactJSON);
                            contacts.put("contacts", contactsData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        editor = preferences.edit();
                        editor.putString("contacts", String.valueOf(contacts));
                        editor.apply();

                        cursor.close();
                        Snackbar.make(findViewById(R.id.fab_addContacts), "Contact Added", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        Snackbar.make(findViewById(R.id.fab_addContacts), "Contact already Added", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }


            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
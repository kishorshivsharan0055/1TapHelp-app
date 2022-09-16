package com.codicts.onetaphelp.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codicts.onetaphelp.Models.EmergencyContact;
import com.codicts.onetaphelp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EmergencyContactsAdapter extends RecyclerView.Adapter<EmergencyContactsAdapter.EmergencyContactsViewHolder> {

    Context mContext;
    private ArrayList<EmergencyContact> contactsList;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    String contactListString;
    JSONArray contactsData = new JSONArray();
    private ArrayList<EmergencyContact> contactsArrayList = new ArrayList<>();

    private int CALL_PERMISSION;

    public class EmergencyContactsViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName, contactPhno;
        public ImageView deleteContactBtn;

        public EmergencyContactsViewHolder(View view) {
            super(view);
            contactName = (TextView) view.findViewById(R.id.contactName);
            contactPhno = (TextView) view.findViewById(R.id.contactPhno);
            deleteContactBtn = (ImageView) view.findViewById(R.id.btnContactDelete);
        }


    }

    public EmergencyContactsAdapter(ArrayList<EmergencyContact> contactsList, Context context) {
        this.contactsList = contactsList;
        this.mContext = context;
        this.preferences = context.getSharedPreferences("com.codicts.onetaphelp", Context.MODE_PRIVATE);
        this.editor = preferences.edit();

        contactListString = preferences.getString("contacts", null);

        if (contactListString != null) {
            try {
                JSONObject contactListJSON = new JSONObject(contactListString);
                contactsData = contactListJSON.getJSONArray("contacts");
                for (int i=0;i<contactsData.length(); i++){
                    EmergencyContact contact = new EmergencyContact();
                    contact.contactName = contactsData.getJSONObject(i).getString("contactName");
                    contact.contactPhno = contactsData.getJSONObject(i).getString("contactPhNo");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @NonNull
    @Override
    public EmergencyContactsAdapter.EmergencyContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new EmergencyContactsViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull EmergencyContactsAdapter.EmergencyContactsViewHolder holder, int position) {
        final EmergencyContact currentContact = contactsList.get(position);
        holder.contactName.setText(currentContact.getContactName());
        holder.contactPhno.setText(currentContact.getContactPhno());
        holder.deleteContactBtn.setOnClickListener(v -> {
            contactsData.remove(position);
            contactsList.remove(position);
            JSONObject contacts = new JSONObject();
            try {
                contacts.put("contacts", contactsData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            editor.putString("contacts", String.valueOf(contacts));
            editor.commit();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, contactsList.size());
            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }
}

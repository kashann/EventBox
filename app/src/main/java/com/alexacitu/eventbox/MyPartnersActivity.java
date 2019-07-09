package com.alexacitu.eventbox;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class MyPartnersActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<User> partners;
    private UserType userType;
    private String id;
    private ArrayList<String> partnersIds, partnersEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_partners);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            userType = null;
        } else {
            userType = (UserType) extras.getSerializable("type");
            if(userType == UserType.CLIENT)
                getSupportActionBar().setTitle(R.string.myEventPlanners);
            else if(userType == UserType.EVENT_PLANNER)
                getSupportActionBar().setTitle(R.string.myClients);
            id = extras.getString("id", null);
        }
        final ListView listView = findViewById(R.id.lvMyClients);
        partners = new ArrayList<>();
        partnersIds = new ArrayList<>();
        partnersEvent = new ArrayList<>();
        db.collection("users").document(id).collection("events")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    Map<String, Object> map = ds.getData();
                    partnersIds.add(map.get("partner")+"");
                    partnersEvent.add(map.get("title")+"");
                }
                db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot ds : queryDocumentSnapshots) {
                            Map<String, Object> map = ds.getData();
                            for(int i = 0; i < partnersIds.size(); i++)
                                if (ds.getId().equals(partnersIds.get(i)))
                                    partners.add(new User(ds.getId(), map.get("firstName") + "", map.get("lastName") + "",
                                            map.get("phone") + "", UserType.valueOf(map.get("type") + ""), partnersEvent.get(i)));
                        }
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CALL_PHONE);
                                if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                                    ActivityCompat.requestPermissions(MyPartnersActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, 123);
                                else
                                    startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + ((User)parent.getItemAtPosition(position)).getNumber())));
                            }
                        });
                        PartnerAdapter customAdapter = new PartnerAdapter(getApplicationContext(), R.layout.partner_row_item, partners);
                        listView.setAdapter(customAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyPartnersActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MyPartnersActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.alexacitu.eventbox;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class EventActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final int MAPS_REQUEST_CODE = 9;
    private GoogleApiClient mClient;
    private Event event;
    private UserType type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            event = null;
            type = null;
        } else {
            event = (Event) extras.getSerializable("event");
            type = (UserType) extras.getSerializable("type");
        }
        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        TextView title = findViewById(R.id.tvEventTitle);
        TextView description = findViewById(R.id.tvEventDescription);
        TextView eventType = findViewById(R.id.tvEventEventType);
        TextView noGuests = findViewById(R.id.tvNoGuests);
        TextView location = findViewById(R.id.tvLocation);
        CalendarView calendar = findViewById(R.id.calendarView);
        final TextView partner = findViewById(R.id.tvPartner);
        title.setText(event.getTitle());
        getSupportActionBar().setTitle(event.getTitle());
        description.setText(event.getDescription());
        eventType.setText("Event type: " + event.getType());
        noGuests.setText("Number of guests: " + event.getNoGuests());
        location.setText("Location: " + event.getLocation());
        calendar.setDate(event.getDate());

        db.collection("users").document(event.getPartnerId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = task.getResult().getData();
                    if(map != null){
                        if(type == UserType.EVENT_PLANNER)
                            partner.setText("Client: " + map.get("firstName") + " " + map.get("lastName"));
                        else if(type == UserType.CLIENT)
                            partner.setText("Event Planner: " + map.get("firstName") + " " + map.get("lastName"));
                    }
                    else {
                        if(type == UserType.EVENT_PLANNER)
                            partner.setText("Client: User no longer exists!");
                        else if(type == UserType.CLIENT)
                            partner.setText("Event Planner: User no longer exists!");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EventActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void navigateToMaps(View view){
        Intent intent = new Intent(EventActivity.this, MapActivity.class);
        intent.putExtra("lat", event.getLatitude());
        intent.putExtra("long", event.getLongitude());
        intent.putExtra("address", event.getLocation());
        startActivityForResult(intent, MAPS_REQUEST_CODE);
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

package com.alexacitu.eventbox;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewEventActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int PLACE_PICKER_REQUEST = 1000;
    private GoogleApiClient mClient;
    private TextView location;
    private CalendarView calendarView;
    private Event event;
    private String title, description, noGuests, locationString, tempLat, tempLong, type, id, eventPlanner;
    private long date;
    private Spinner spinnerEventType, spinnerEventPlanner;
    private ArrayList<User> eventPlanners;
    private ArrayList<String> eventPlannersNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            id = null;
        } else {
            id = extras.getString("id");
        }
        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        location = findViewById(R.id.tvLocation);
        calendarView = findViewById(R.id.calendarViewNewEvent);
        tempLat = null;
        tempLong = null;
        eventPlanners = new ArrayList<>();
        eventPlannersNames = new ArrayList<>();
        spinnerEventType = findViewById(R.id.spinnerEventType);
        spinnerEventType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, EventType.values()));
        spinnerEventPlanner = findViewById(R.id.spinnerEventPlanner);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = new GregorianCalendar();
                calendar.set(year, month, dayOfMonth);
                date = calendar.getTimeInMillis();
            }
        });

        db.collection("users").whereEqualTo("type", "EVENT_PLANNER")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    Map<String, Object> map = ds.getData();
                    User user = new User(ds.getId(), map.get("firstName")+"", map.get("lastName")+"",
                            map.get("phone")+"", UserType.valueOf(map.get("type")+""), null);
                    eventPlanners.add(user);
                    eventPlannersNames.add(user.getFirstName() + " " + user.getLastName());
                }
                spinnerEventPlanner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, eventPlannersNames));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewEventActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void submitNewEvent(View v){
        title = ((EditText)findViewById(R.id.etTitle)).getText().toString();
        description = ((EditText)findViewById(R.id.etDescription)).getText().toString();
        noGuests = ((EditText)findViewById(R.id.etNoGuests)).getText().toString();
        locationString = location.getText().toString();
        type = spinnerEventType.getSelectedItem().toString();
        eventPlanner = eventPlanners.get(spinnerEventPlanner.getSelectedItemPosition()).getId();
        if(title.isEmpty() || description.isEmpty() || tempLat == null || tempLong == null || noGuests.isEmpty() || locationString.equals(R.string.location)){
            Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        String randomEventId = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        event = new Event(randomEventId, title, description, date, locationString, tempLat, tempLong, Integer.parseInt(noGuests), EventType.valueOf(type), eventPlanner);
        Map<String, Object> firestoreCreatorEvent = new HashMap<>();
        firestoreCreatorEvent.put("title", event.getTitle());
        firestoreCreatorEvent.put("description", event.getDescription());
        firestoreCreatorEvent.put("date", event.getDate());
        firestoreCreatorEvent.put("location", event.getLocation());
        firestoreCreatorEvent.put("latitude", event.getLatitude());
        firestoreCreatorEvent.put("longitude", event.getLongitude());
        firestoreCreatorEvent.put("noGuests", event.getNoGuests());
        firestoreCreatorEvent.put("type", event.getType());
        firestoreCreatorEvent.put("partner", event.getPartnerId());
        Map<String, Object> firestorePlannerEvent = new HashMap<>();
        firestorePlannerEvent.put("title", event.getTitle());
        firestorePlannerEvent.put("description", event.getDescription());
        firestorePlannerEvent.put("date", event.getDate());
        firestorePlannerEvent.put("location", event.getLocation());
        firestorePlannerEvent.put("latitude", event.getLatitude());
        firestorePlannerEvent.put("longitude", event.getLongitude());
        firestorePlannerEvent.put("noGuests", event.getNoGuests());
        firestorePlannerEvent.put("type", event.getType());
        firestorePlannerEvent.put("partner", id);

        WriteBatch batch = db.batch();
        DocumentReference creatorRef = db.collection("users").document(id)
                .collection("events").document(event.getId());
        batch.set(creatorRef, firestoreCreatorEvent);
        DocumentReference plannerRef = db.collection("users").document(event.getPartnerId())
                .collection("events").document(event.getId());
        batch.set(plannerRef, firestorePlannerEvent);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(NewEventActivity.this, "Event added!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewEventActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void placePicker(View v){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(NewEventActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String placename = String.format("%s", place.getName());
                String latitude = String.valueOf(place.getLatLng().latitude);
                String longitude = String.valueOf(place.getLatLng().longitude);
                String address = String.format("%s", place.getAddress());
                location.setText(placename + " => " + address);
                tempLat = latitude;
                tempLong = longitude;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }
    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
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

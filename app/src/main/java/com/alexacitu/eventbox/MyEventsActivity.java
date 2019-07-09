package com.alexacitu.eventbox;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.layout.Document;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MyEventsActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private UserType type;
    private String id;
    private ArrayList<Event> events;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            type = null;
        } else {
            type = (UserType) extras.getSerializable("type");
            id = extras.getString("id", null);
        }
        events = new ArrayList<>();
        final ListView listView = findViewById(R.id.lvMyEvents);
        final EditText filter = findViewById(R.id.searchBar);
        db.collection("users").document(id).collection("events")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot ds : queryDocumentSnapshots) {
                    Map<String, Object> map = ds.getData();
                    events.add(new Event(ds.getId(), map.get("title") + "", map.get("description") + "", Long.parseLong(map.get("date")+""),
                            map.get("location") + "", map.get("latitude") + "", map.get("longitude") + "",
                            Integer.parseInt(map.get("noGuests") + ""), EventType.valueOf(map.get("type") + ""), map.get("partner") + ""));
                }
                final EventAdapter customAdapter = new EventAdapter(getApplicationContext(), R.layout.event_row_item, events);
                listView.setAdapter(customAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), EventActivity.class);
                        intent.putExtra("event", (Event)parent.getItemAtPosition(position));
                        intent.putExtra("type", type);
                        startActivity(intent);
                    }
                });
                filter.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        customAdapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MyEventsActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.btnPdf:
                createPdf();
                break;
            case R.id.btnChart:
                Intent intent = new Intent(this, ChartActivity.class);
                intent.putExtra("events", events);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createPdf(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MyEventsActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        else if(events.isEmpty()){
            Toast.makeText(this, "No events to generate report from!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            String directory_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/EventBox/events.pdf";
            try{
                PdfWriter pdfWriter = new PdfWriter(directory_path);
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                Document document = new Document(pdfDocument);

                for(int i = 0; i < events.size(); i++){
                    Table table = new Table(2);
                    table.addCell("Event #" + (i + 1));
                    table.addCell("");
                    table.addCell("Title");
                    table.addCell(events.get(i).getTitle());
                    table.addCell("Description");
                    table.addCell(events.get(i).getDescription());
                    table.addCell("Date");
                    table.addCell(new Date(events.get(i).getDate()).toString());
                    table.addCell("Location");
                    String location = Normalizer.normalize(events.get(i).getLocation(), Normalizer.Form.NFD);
                    location = location.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    table.addCell(location);
                    table.addCell("Number of Guests");
                    table.addCell(events.get(i).getNoGuests()+"");
                    table.addCell("Event Type");
                    table.addCell(events.get(i).getType().toString());
                    document.add(table);
                    document.add(new Paragraph("\n\n"));
                }
                document.close();
                Toast.makeText(this, "File saved successfully in 'Downloads/EventBox' folder!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
                Toast.makeText(this, "Error! File not found Exception occurred!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error! Exception occurred!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
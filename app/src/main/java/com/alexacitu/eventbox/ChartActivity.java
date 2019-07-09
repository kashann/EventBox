package com.alexacitu.eventbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChartActivity extends AppCompatActivity {
    private ArrayList<Event> events;
    private BarChart chart;
    private Spinner yearSpinner;
    private int months[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            events = new ArrayList<>();
        } else {
            events = (ArrayList<Event>) extras.getSerializable("events");
        }
        chart = findViewById(R.id.barChart);
        final ArrayList<String> labels = new ArrayList<>();
        labels.add("Jan");
        labels.add("Feb");
        labels.add("Mar");
        labels.add("Apr");
        labels.add("May");
        labels.add("Jun");
        labels.add("Jul");
        labels.add("Aug");
        labels.add("Sep");
        labels.add("Oct");
        labels.add("Nov");
        labels.add("Dec");
        Date currentTime = new Date(System.currentTimeMillis());
        final ArrayList<String> years = new ArrayList<>();
        final HashMap<String, Long> yearsMillis = new HashMap();
        for(int i = 0; i < 5; i++){ //get the last 5 years
            years.add((currentTime.getYear() - i + 1900) + "");
            yearsMillis.put((currentTime.getYear() - i + 1900) + "",
                    getMilliFromDate("01/01/" + (currentTime.getYear() - i + 1900)));
        }
        yearSpinner = findViewById(R.id.spinnerChartYear);
        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years));
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                months = new int[12];
                Date selection = new Date(yearsMillis.get(years.get(position)));
                for(int i = 0; i < events.size(); i++){
                    Date eventDate = new Date(events.get(i).getDate());
                    if(eventDate.getYear() == selection.getYear())
                        months[eventDate.getMonth() - 1]++;
                }
                ArrayList<BarEntry> entries = new ArrayList<>();
                for(int i = 0; i < months.length; i++)
                    entries.add(new BarEntry(months[i], i));
                BarDataSet barDataSet = new BarDataSet(entries, "Number of events per Month");
                BarData data = new BarData(labels, barDataSet);
                chart.setData(data);
                barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                chart.animateY(2000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    public long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}

package com.alexacitu.eventbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    public static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            user = null;
        } else {
            user = (User)extras.getSerializable("user");
        }
        Button btnAddEvent = findViewById(R.id.btnAddEvent);
        Button btnMyPartners = findViewById(R.id.btnMyPartners);
        Button btnToDos = findViewById(R.id.btnToDo);
        TextView title = findViewById(R.id.tvTitle);
        TextView subTitle = findViewById(R.id.tvSubtitle);
        title.setText("Hello, dear " + user.getFirstName() + "!");
        if(user.getUserType() == UserType.CLIENT){
            btnAddEvent.setVisibility(View.VISIBLE);
            btnMyPartners.setText(R.string.myEventPlanners);
            subTitle.setText(R.string.subtitleClient);
        }
        else if(user.getUserType() == UserType.EVENT_PLANNER){
            btnMyPartners.setText(R.string.myClients);
            btnToDos.setVisibility(View.VISIBLE);
            subTitle.setText(R.string.subtitleEventPlanner);
        }
    }

    public void myEvents(View v){
        Intent intent = new Intent(this, MyEventsActivity.class);
        intent.putExtra("type", user.getUserType());
        intent.putExtra("id", user.getId());
        startActivity(intent);
    }

    public void addEvent(View v){
        Intent intent = new Intent(this, NewEventActivity.class);
        intent.putExtra("id", user.getId());
        startActivity(intent);
    }

    public void myPartners(View v){
        Intent intent = new Intent(this, MyPartnersActivity.class);
        intent.putExtra("type", user.getUserType());
        intent.putExtra("id", user.getId());
        startActivity(intent);
    }

    public void toDoList(View v){
        Intent intent = new Intent(this, ToDosActivity.class);
        intent.putExtra("toDos", user.getToDos());
        intent.putExtra("id", user.getId());
        startActivity(intent);
    }

    public void logout(View v){
        FirebaseAuth.getInstance().signOut();
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}

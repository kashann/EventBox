package com.alexacitu.eventbox;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ToDosActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static ArrayList<ToDo> toDos;
    public static ListView lvToDos;
    public static ToDosAdapter arrayAdapter;
    public static String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_dos);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lvToDos = findViewById(R.id.lvToDos);
        toDos = new ArrayList<>();
        lvToDos.setAdapter(null);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            toDos = null;
            id = null;
        } else {
            toDos = (ArrayList<ToDo>) extras.getSerializable("toDos");
            id = extras.getString("id");
        }

        lvToDos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvToDos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean done = ((ToDo)lvToDos.getItemAtPosition(position)).isDone();
                lvToDos.setItemChecked(position, !done);
                toDos.get(position).setDone(!done);
                db.collection("users").document(ToDosActivity.id).collection("toDos")
                        .document(position+"").set(toDos.get(position))
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Log.w("WTF", e.toString());
                            }
                        });

            }
        });
        arrayAdapter = new ToDosAdapter(this, android.R.layout.simple_list_item_checked, toDos);
        lvToDos.setAdapter(arrayAdapter);
        for(int i = 0; i < ToDosActivity.toDos.size(); i++){
            boolean done = ((ToDo)lvToDos.getItemAtPosition(i)).isDone();
            lvToDos.setItemChecked(i, done);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addtodo_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.btnAddToDo:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment dialogFragment = new NewToDoDialogFragment();
                dialogFragment.show(ft, "dialog");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
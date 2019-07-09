package com.alexacitu.eventbox;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public User user;
    public ArrayList<ToDo> toDos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toDos.add(new ToDo("Contact sponsors"));
        toDos.add(new ToDo("Contact retailers"));
        toDos.add(new ToDo("Make guest list"));
        toDos.add(new ToDo("Hire staff"));
        toDos.add(new ToDo("Order flower arrangements"));
        toDos.add(new ToDo("Establish the menu"));
        toDos.add(new ToDo("Ensure catering"));
        toDos.add(new ToDo("Order equipment"));
        toDos.add(new ToDo("Order the cake"));
        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View v){
        final String fName = ((EditText)findViewById(R.id.etFirstName)).getText().toString();
        final String lName = ((EditText)findViewById(R.id.etLastName)).getText().toString();
        final String email = ((EditText)findViewById(R.id.etRegisterEmail)).getText().toString();
        final String phone = ((EditText)findViewById(R.id.etPhoneNumber)).getText().toString();
        final String password = ((EditText)findViewById(R.id.etRegisterPassword)).getText().toString();
        String verifyPassword = ((EditText)findViewById(R.id.etRegisterVerifyPassword)).getText().toString();
        RadioGroup radioGroup = findViewById(R.id.rgUserType);
        final RadioButton client = findViewById(R.id.rbClient);
        final RadioButton eventPlanner = findViewById(R.id.rbEventPlanner);

        if(fName.isEmpty() || lName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || verifyPassword.isEmpty()){
            Toast.makeText(this, "Please complete all the fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(!password.equals(verifyPassword)){
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(phone.length() < 10){
            Toast.makeText(this, "Phone number incomplete!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(password.length() < 6){
            Toast.makeText(this, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (radioGroup.getCheckedRadioButtonId() == -1)
        {
            Toast.makeText(this, "Please select a user type!", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                user = new User (firebaseUser.getUid(), fName, lName, email, password, phone,
                                        client.isChecked() ? UserType.CLIENT : eventPlanner.isChecked() ? UserType.EVENT_PLANNER : null, toDos);
                                Map<String, Object> firestoreUser = new HashMap<>();
                                firestoreUser.put("firstName", user.getFirstName());
                                firestoreUser.put("lastName", user.getLastName());
                                firestoreUser.put("phone", user.getNumber());
                                firestoreUser.put("type", user.getUserType());

                                WriteBatch batch = db.batch();
                                DocumentReference usersRef = db.collection("users").document(user.getId());
                                batch.set(usersRef, firestoreUser);

                                if(user.getUserType() == UserType.EVENT_PLANNER){
                                    Map<String, Object> firestoreToDo = new HashMap<>();
                                    for(int i = 0; i < user.getToDos().size(); i++){
                                        firestoreToDo.put("title", user.getToDos().get(i).getTitle());
                                        firestoreToDo.put("done", user.getToDos().get(i).isDone());
                                        batch.set(usersRef.collection("toDos").document(i+""), firestoreToDo);
                                    }
                                }

                                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(SignUpActivity.this, MenuActivity.class);
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    }
                                });
                            } else {
                                Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
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

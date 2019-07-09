package com.alexacitu.eventbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User user;
    private FirebaseUser fUser;
    private DocumentReference docRef;
    private ArrayList<ToDo> toDos;
    private CheckBox remember;
    private SharedPreferences mPrefs;
    private static final String PREFS_NAME = "PrefsFile";
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        remember = findViewById(R.id.cbRemember);
        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etEmail = findViewById(R.id.etLogInEmail);
        etPassword = findViewById(R.id.etLogInPassword);
        if(mPrefs.contains("pref_email")){
            String e = mPrefs.getString("pref_email", "");
            etEmail.setText(e);
        }
        if(mPrefs.contains("pref_password")){
            String p = mPrefs.getString("pref_password", "");
            etPassword.setText(p);
        }
        if(mPrefs.contains("pref_password")){
            Boolean c = mPrefs.getBoolean("pref_checked", false);
            remember.setChecked(c);
        }
    }

    public void authenticate(View v){
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        if(email.isEmpty()){
            Toast.makeText(this, "No Email entered!", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(password.isEmpty()){
            Toast.makeText(this, "No password entered!", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            fUser = mAuth.getCurrentUser();
                            user = new User();
                            toDos = new ArrayList<>();
                            final Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            docRef = db.collection("users").document(fUser.getUid());
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> map = task.getResult().getData();
                                        user = new User(fUser.getUid(), map.get("firstName")+"", map.get("lastName")+"",
                                                map.get("phone")+"", UserType.valueOf(map.get("type")+""), null);
                                        docRef.collection("toDos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Map<String, Object> firestoreToDo = document.getData();
                                                        String title = firestoreToDo.get("title").toString();
                                                        String done = firestoreToDo.get("done").toString();
                                                        ToDo toDo = new ToDo(title, Boolean.parseBoolean(done));
                                                        toDos.add(toDo);
                                                    }
                                                    user.setToDos(toDos);
                                                    if(remember.isChecked()){
                                                        Boolean boolIsChecked = remember.isChecked();
                                                        SharedPreferences.Editor editor = mPrefs.edit();
                                                        editor.putString("pref_email", email);
                                                        editor.putString("pref_password", password);
                                                        editor.putBoolean("pref_checked", boolIsChecked);
                                                        editor.apply();
                                                    }
                                                    else {
                                                        mPrefs.edit().clear().apply();
                                                    }
                                                    intent.putExtra("user", user);
                                                    startActivity(intent);
                                                } else {
                                                    Log.wtf("WTF", "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            Log.wtf("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void signUp(View v){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
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

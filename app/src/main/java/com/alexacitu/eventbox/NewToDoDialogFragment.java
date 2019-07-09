package com.alexacitu.eventbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;


public class NewToDoDialogFragment extends DialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText newToDo;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View DialogView = inflater.inflate(R.layout.fragment_new_to_do_dialog,null);
        newToDo = DialogView.findViewById(R.id.etNewToDo);
        b.setView(DialogView)
                .setTitle(R.string.addNewToDo)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String text = newToDo.getText().toString();
                        if(text.isEmpty()){
                            Toast.makeText(getContext(), "Field is empty!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final ToDo toDo = new ToDo(newToDo.getText().toString());
                        db.collection("users").document(ToDosActivity.id).collection("toDos")
                            .document(ToDosActivity.toDos.size()+"").set(toDo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    ToDosActivity.toDos.add(toDo);
                                    ToDosActivity.arrayAdapter.notifyDataSetChanged();
                                    MenuActivity.user.addToDo(toDo);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    Log.w("WTF", e.toString());
                                }
                            });
                    }
                });
        return b.create();
    }
}

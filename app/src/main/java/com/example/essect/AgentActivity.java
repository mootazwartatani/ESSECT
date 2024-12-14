package com.example.essect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AgentActivity extends BaseActivity {

    private static final String TAG = "AgentActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent);


        db = FirebaseFirestore.getInstance();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        LinearLayout teacherListLayout = findViewById(R.id.teacherListLayout);


        fetchTeachers(teacherListLayout);
    }


    private void fetchTeachers(LinearLayout linearLayout) {
        db.collection("users")
                .whereEqualTo("role", "teacher")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> teacherIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String teacherName = document.getString("name");
                            String teacherId = document.getId();


                            teacherIds.add(teacherId);


                            fetchTeacherAbsenceCount(teacherId, teacherName, linearLayout);
                        }

                        if (teacherIds.isEmpty()) {
                            Toast.makeText(this, "Aucun enseignant trouvé", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des enseignants", task.getException());
                        Toast.makeText(this, "Erreur lors de la récupération des enseignants", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchTeacherAbsenceCount(String teacherId, String teacherName, LinearLayout linearLayout) {
        db.collection("absences")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int absenceCount = task.getResult().size();


                        Button teacherButton = new Button(this);
                        teacherButton.setText(teacherName + " - Absences : " + absenceCount);
                        teacherButton.setOnClickListener(v -> {

                            Intent intent = new Intent(AgentActivity.this, AddAbsenceActivity.class);
                            intent.putExtra("teacherId", teacherId);
                            intent.putExtra("teacherName", teacherName);
                            startActivity(intent);
                        });


                        linearLayout.addView(teacherButton);
                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des absences pour " + teacherName, task.getException());
                        Toast.makeText(this, "Erreur lors de la récupération des absences pour " + teacherName, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
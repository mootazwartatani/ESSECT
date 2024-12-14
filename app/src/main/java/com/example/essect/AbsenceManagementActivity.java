package com.example.essect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbsenceManagementActivity extends AppCompatActivity {

    private static final String TAG = "AbsenceManagement";
    private FirebaseFirestore db;
    private List<Map<String, String>> absences;
    private AbsenceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_management);


        db = FirebaseFirestore.getInstance();


        ListView absenceListView = findViewById(R.id.absenceListView);


        absences = new ArrayList<>();
        adapter = new AbsenceAdapter(this, absences);
        absenceListView.setAdapter(adapter);

        fetchAbsences();


        absenceListView.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> selectedAbsence = absences.get(position);


            Intent intent = new Intent(AbsenceManagementActivity.this, AbsenceDetailActivity.class);
            intent.putExtra("teacherName", selectedAbsence.get("teacherName"));
            intent.putExtra("date", selectedAbsence.get("date"));
            intent.putExtra("time", selectedAbsence.get("time"));
            intent.putExtra("room", selectedAbsence.get("room"));
            startActivity(intent);
        });
    }

    private void fetchAbsences() {
        db.collection("absences")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        absences.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> absence = new HashMap<>();
                            absence.put("teacherName", document.getString("teacherName"));
                            absence.put("date", document.getString("date"));
                            absence.put("time", document.getString("time"));
                            absence.put("room", document.getString("room"));
                            absences.add(absence);
                        }
                        adapter.notifyDataSetChanged();
                        if (absences.isEmpty()) {
                            Toast.makeText(this, "Aucune absence enregistrée", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des absences", task.getException());
                        Toast.makeText(this, "Erreur lors de la récupération des absences", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
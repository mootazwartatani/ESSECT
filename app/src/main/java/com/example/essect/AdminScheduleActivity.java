package com.example.essect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminScheduleActivity extends AppCompatActivity {

    private static final String TAG = "AdminScheduleActivity";
    private FirebaseFirestore db;
    private RecyclerView scheduleRecyclerView;
    private ScheduleAdapter adapter;
    private List<Map<String, Object>> scheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_schedule);

        db = FirebaseFirestore.getInstance();
        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        Button addScheduleButton = findViewById(R.id.addScheduleButton);

        scheduleList = new ArrayList<>();
        adapter = new ScheduleAdapter(this, scheduleList);

        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(adapter);


        fetchSchedules();


        addScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminScheduleActivity.this, AddScheduleActivity.class);
            startActivity(intent);
        });
    }

    private void fetchSchedules() {
        db.collection("schedules")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scheduleList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> schedule = document.getData();
                            scheduleList.add(schedule);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des emplois du temps", task.getException());
                        Toast.makeText(this, "Erreur lors du chargement des emplois du temps", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
package com.example.essect;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AbsenceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_detail);


        String teacherName = getIntent().getStringExtra("teacherName");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String room = getIntent().getStringExtra("room");


        TextView teacherNameTextView = findViewById(R.id.teacherNameTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView roomTextView = findViewById(R.id.roomTextView);

        teacherNameTextView.setText("Enseignant: " + teacherName);
        dateTextView.setText("Date: " + date);
        timeTextView.setText("Heure: " + time);
        roomTextView.setText("Salle: " + room);
    }
}
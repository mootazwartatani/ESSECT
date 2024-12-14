package com.example.essect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class UserAbsenceActivity extends AppCompatActivity {

    private static final String TAG = "UserAbsenceActivity";
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LinearLayout absenceLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_absence);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        absenceLinearLayout = findViewById(R.id.absenceLinearLayout);


        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();


        subscribeToUserTopic(userId);


        fetchUserAbsences(userId);
    }

    private void subscribeToUserTopic(String userId) {
        FirebaseMessaging.getInstance().subscribeToTopic("teacher_" + userId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Abonnement au topic réussi : teacher_" + userId);
                        Toast.makeText(this, "Abonnement aux notifications activé.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Erreur lors de l'abonnement au topic : " + task.getException());
                    }
                });
    }

    private void fetchUserAbsences(String userId) {
        db.collection("absences")
                .whereEqualTo("teacherId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            displayNoAbsencesMessage();
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String date = document.getString("date");
                            String time = document.getString("time");
                            String room = document.getString("room");


                            createAbsenceCard(date, time, room);
                        }
                    } else {
                        Log.e(TAG, "Erreur lors de la récupération des absences", task.getException());
                        Toast.makeText(this, "Erreur lors de la récupération des absences", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createAbsenceCard(String date, String time, String room) {
        TextView card = new TextView(this);
        card.setText(String.format("Date : %s\nHeure : %s\nSalle : %s", date, time, room));
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        card.setTextSize(16);
        card.setTextColor(getResources().getColor(android.R.color.black));
        card.setElevation(8);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);


        absenceLinearLayout.addView(card);
    }

    private void displayNoAbsencesMessage() {
        TextView noAbsencesMessage = new TextView(this);
        noAbsencesMessage.setText("Aucune absence n'a été enregistrée pour le moment. Si vous pensez que c'est une erreur, veuillez contacter l'administration.");
        noAbsencesMessage.setPadding(16, 16, 16, 16);
        noAbsencesMessage.setTextSize(16);
        noAbsencesMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
        noAbsencesMessage.setGravity(android.view.Gravity.CENTER);

        absenceLinearLayout.addView(noAbsencesMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            auth.signOut();
            Toast.makeText(this, "Vous êtes déconnecté", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
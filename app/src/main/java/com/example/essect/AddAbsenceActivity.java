package com.example.essect;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class AddAbsenceActivity extends AppCompatActivity {

    private static final String TAG = "AddAbsenceActivity";
    private FirebaseFirestore db;
    private String teacherId;
    private String teacherName;
    private String teacherToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_absence);


        db = FirebaseFirestore.getInstance();

        teacherId = getIntent().getStringExtra("teacherId");
        teacherName = getIntent().getStringExtra("teacherName");
        teacherToken = getIntent().getStringExtra("teacherToken");


        EditText dateInput = findViewById(R.id.dateInput);
        EditText timeInput = findViewById(R.id.timeInput);
        EditText roomInput = findViewById(R.id.roomInput);
        Button submitButton = findViewById(R.id.submitButton);


        submitButton.setOnClickListener(v -> {
            String date = dateInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            String room = roomInput.getText().toString().trim();

            if (validateInputs(date, time, room)) {
                checkIfAbsenceExists(date, time, room);
            }
        });
    }

    private boolean validateInputs(String date, String time, String room) {
        if (date.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une heure", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (room.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une salle", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkIfAbsenceExists(String date, String time, String room) {

        Query query = db.collection("absences")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("date", date)
                .whereEqualTo("time", time)
                .whereEqualTo("room", room);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Toast.makeText(this, "Cette absence existe déjà !", Toast.LENGTH_SHORT).show();
                    } else {

                        addAbsenceToFirestore(date, time, room);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la vérification de l'absence", e);
                    Toast.makeText(this, "Erreur lors de la vérification de l'absence", Toast.LENGTH_SHORT).show();
                });
    }

    private void addAbsenceToFirestore(String date, String time, String room) {

        Map<String, Object> absence = new HashMap<>();
        absence.put("teacherId", teacherId);
        absence.put("teacherName", teacherName);
        absence.put("date", date);
        absence.put("time", time);
        absence.put("room", room);


        db.collection("absences")
                .add(absence)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Absence ajoutée avec succès : " + documentReference.getId());


                    incrementTeacherAbsenceCount();


                    sendNotificationToTeacher(teacherToken ,teacherId);

                    Toast.makeText(this, "Absence ajoutée avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de l'ajout de l'absence", e);
                    Toast.makeText(this, "Erreur lors de l'ajout de l'absence", Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementTeacherAbsenceCount() {

        db.collection("users")
                .document(teacherId)
                .update("absenceCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Absence count updated successfully for teacher: " + teacherId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la mise à jour du compteur d'absences", e);
                });
    }

    private void sendNotificationToTeacher(String fcmToken,String userId) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.e(TAG, "FCM Token manquant, notification non envoyée.");

        }
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
}

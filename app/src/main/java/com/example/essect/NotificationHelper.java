package com.example.essect;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static void sendNotificationToTeacher(String teacherToken, String title, String body) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("to", teacherToken);
        notification.put("notification", new HashMap<String, String>() {{
            put("title", title);
            put("body", body);
        }});

        db.collection("notifications").add(notification)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Notification envoyée avec succès"))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de l'envoi de la notification", e));
    }
}
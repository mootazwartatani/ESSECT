package com.example.essect;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerText = findViewById(R.id.registerText);


        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un email et un mot de passe", Toast.LENGTH_SHORT).show();
                return;
            }

            signInWithEmailAndPassword(email, password);
        });


        registerText.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, Signup.class);
            startActivity(intent);
        });

    }


    private void signInWithEmailAndPassword(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            fetchUserRoleAndNavigate(user.getUid());
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchUserRoleAndNavigate(String userId) {
        Log.d("FirebaseUID", "Fetching data for UID: " + userId);

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("FirestoreData", "Document data: " + documentSnapshot.getData());
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            navigateToRoleSpecificUI(role.trim().toLowerCase());
                        } else {
                            Toast.makeText(MainActivity.this, "Rôle utilisateur non spécifié", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Utilisateur non trouvé dans Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Erreur lors de la récupération des données : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void navigateToRoleSpecificUI(String role) {
        if (role == null || role.isEmpty()) {
            Toast.makeText(this, "Rôle utilisateur inconnu", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;

        switch (role) {
            case "admin":
                intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                break;
            case "agent":
                intent = new Intent(MainActivity.this, AgentActivity.class);
                break;
            case "teacher":
                intent = new Intent(MainActivity.this, UserAbsenceActivity.class);
                break;
            default:
                Toast.makeText(this, "Rôle utilisateur inconnu", Toast.LENGTH_SHORT).show();
                return;
        }

        startActivity(intent);
    }
}
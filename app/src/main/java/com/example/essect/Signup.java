package com.example.essect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private static final String TAG = "Signup";
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText nameInput = findViewById(R.id.nameInput);
        Button signUpButton = findViewById(R.id.signUpButton);
        Button goToLoginButton = findViewById(R.id.goToLoginButton);


        signUpButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();


            if (!validateInputs(email, password, name)) return;

            String role = "teacher";
            registerUser(email, password, name, role);
        });


        goToLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(Signup.this, MainActivity.class));
            finish();
        });
    }

    private boolean validateInputs(String email, String password, String name) {
        if (email.isEmpty()) {
            showError("Veuillez entrer un email valide", R.id.emailInput);
            return false;
        }
        if (password.isEmpty()) {
            showError("Veuillez entrer un mot de passe", R.id.passwordInput);
            return false;
        }
        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères", R.id.passwordInput);
            return false;
        }
        if (name.isEmpty()) {
            showError("Veuillez entrer votre nom", R.id.nameInput);
            return false;
        }
        return true;
    }

    private void showError(String message, int viewId) {
        EditText view = findViewById(viewId);
        view.setError(message);
        view.requestFocus();
    }

    private void registerUser(String email, String password, String name, String role) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        Log.d(TAG, "Utilisateur créé avec l'UID : " + userId);


                        addUserToFirestore(userId, name, email, role);
                    } else {
                        Log.e(TAG, "Erreur lors de la création de l'utilisateur", task.getException());
                        Toast.makeText(this, "Erreur lors de l'inscription : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addUserToFirestore(String userId, String name, String email, String role) {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Échec de la récupération du token FCM", task.getException());
                        return;
                    }

                    String fcmToken = task.getResult();


                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("email", email);
                    user.put("role", role);
                    user.put("fcmToken", fcmToken); // Ajouter le token FCM


                    db.collection("users").document(userId).set(user)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Utilisateur enregistré avec succès dans Firestore avec l'UID : " + userId);
                                Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                                navigateToHome();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de l'ajout dans Firestore : " + e.getMessage(), e);
                                Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(Signup.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
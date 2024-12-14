package com.example.essect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = "AdminHomeActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        welcomeTextView = findViewById(R.id.welcomeTextView);
        Button viewAbsencesButton = findViewById(R.id.viewAbsencesButton);
        Button manageSchedulesButton = findViewById(R.id.manageSchedulesButton);
        Button viewReportsButton = findViewById(R.id.viewReportsButton);


        loadAdminName();


        viewAbsencesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AbsenceManagementActivity.class);
            startActivity(intent);
        });


        manageSchedulesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminScheduleActivity.class);
            startActivity(intent);
        });


        viewReportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, ReportsActivity.class);
            startActivity(intent);
        });
    }


    private void loadAdminName() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid != null) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null) {
                                welcomeTextView.setText("Bienvenue, " + name);
                            } else {
                                welcomeTextView.setText("Bienvenue, Administrateur");
                            }
                        } else {
                            welcomeTextView.setText("Bienvenue, Administrateur");
                            Log.e(TAG, "Document introuvable pour UID : " + uid);
                        }
                    })
                    .addOnFailureListener(e -> {
                        welcomeTextView.setText("Bienvenue, Administrateur");
                        Log.e(TAG, "Erreur Firestore : " + e.getMessage(), e);
                    });
        } else {
            Toast.makeText(this, "Session expirée, veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Vous êtes déconnecté", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }


    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
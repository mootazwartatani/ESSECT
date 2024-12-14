package com.example.essect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddScheduleActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private FirebaseFirestore db;
    private EditText dayEditText, timeEditText, teacherEditText, classEditText;
    private Button saveScheduleButton, importFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);


        checkPermissions();

        db = FirebaseFirestore.getInstance();


        dayEditText = findViewById(R.id.dayEditText);
        timeEditText = findViewById(R.id.timeEditText);
        teacherEditText = findViewById(R.id.teacherEditText);
        classEditText = findViewById(R.id.classEditText);
        saveScheduleButton = findViewById(R.id.saveScheduleButton);
        importFileButton = findViewById(R.id.importFileButton);


        saveScheduleButton.setOnClickListener(v -> saveSchedule());


        importFileButton.setOnClickListener(v -> openFileChooser());
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                showPermissionRationale();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Permissions nécessaires")
                .setMessage("Cette application nécessite l'accès au stockage pour importer ou enregistrer des fichiers.")
                .setPositiveButton("OK", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this,
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void redirectToSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions accordées", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permissions nécessaires")
                        .setMessage("Les permissions sont nécessaires pour importer des fichiers. Veuillez les activer dans les paramètres.")
                        .setPositiveButton("Ouvrir les paramètres", (dialog, which) -> redirectToSettings())
                        .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }

    private void saveSchedule() {
        String day = dayEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();
        String teacher = teacherEditText.getText().toString().trim();
        String className = classEditText.getText().toString().trim();

        if (day.isEmpty() || time.isEmpty() || teacher.isEmpty() || className.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("day", day);
        schedule.put("time", time);
        schedule.put("teacher", teacher);
        schedule.put("class", className);

        db.collection("schedules")
                .add(schedule)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Emploi du temps ajouté avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'ajout de l'emploi du temps : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("/");
        String[] mimeTypes = {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/pdf"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez un fichier"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();

            if (fileUri != null) {
                String fileType = getContentResolver().getType(fileUri);

                if (fileType != null && fileType.contains("spreadsheet")) {
                    processExcelFile(fileUri);
                } else if (fileType != null && fileType.contains("pdf")) {
                    processPdfFile(fileUri);
                } else {
                    Toast.makeText(this, "Format de fichier non supporté", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void processExcelFile(Uri fileUri) {
        try {
            List<Map<String, String>> schedules = ExcelReader.readExcelFile(this, fileUri);
            for (Map<String, String> schedule : schedules) {
                db.collection("schedules").add(schedule)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Emploi du temps ajouté : " + schedule, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Erreur lors de l'ajout : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
            Toast.makeText(this, "Fichier Excel traité avec succès", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors du traitement du fichier Excel : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void processPdfFile(Uri fileUri) {
        try {
            String pdfContent = PdfReaderHelper.readPdfFile(this, fileUri);
            Toast.makeText(this, "Contenu du PDF traité avec succès", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors du traitement du fichier PDF : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
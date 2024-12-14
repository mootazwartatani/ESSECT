package com.example.essect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;

import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private static final String TAG = "ReportsActivity";
    private FirebaseFirestore db;
    private BarChart barChart;
    private String startDate = null;
    private String endDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);


        db = FirebaseFirestore.getInstance();


        barChart = findViewById(R.id.chart);


        Button reportByTeacherButton = findViewById(R.id.reportByTeacherButton);
        Button reportByRoomButton = findViewById(R.id.reportByRoomButton);
        Button reportByPeriodButton = findViewById(R.id.reportByPeriodButton);


        reportByTeacherButton.setOnClickListener(v -> fetchReportByTeacher());


        reportByRoomButton.setOnClickListener(v -> fetchReportByRoom());


        reportByPeriodButton.setOnClickListener(v -> {
            if (startDate == null || endDate == null) {
                showDatePickers();
            } else {
                fetchReportByPeriod(startDate, endDate);
            }
        });
    }


    private void showDatePickers() {
        showDatePickerDialog(date -> {
            startDate = date;
            showDatePickerDialog(endDate -> {
                this.endDate = endDate;
                fetchReportByPeriod(startDate, endDate);
            });
        });
    }


    private void fetchReportByTeacher() {
        db.collection("absences").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> absencesByTeacher = new HashMap<>();

                    queryDocumentSnapshots.forEach(document -> {
                        String teacherName = document.getString("teacherName");
                        if (teacherName != null) {
                            absencesByTeacher.put(teacherName, absencesByTeacher.getOrDefault(teacherName, 0) + 1);
                        }
                    });


                    displayBarChart("Absences par Enseignant", absencesByTeacher);
                    String reportContent = generateProfessionalReport("Rapport par Enseignant", absencesByTeacher, null);
                    showReportDialog("Rapport par Enseignant", reportContent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erreur Firestore : " + e.getMessage(), e);
                });
    }


    private void fetchReportByRoom() {
        db.collection("absences").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> absencesByRoom = new HashMap<>();

                    queryDocumentSnapshots.forEach(document -> {
                        String room = document.getString("room");
                        if (room != null) {
                            absencesByRoom.put(room, absencesByRoom.getOrDefault(room, 0) + 1);
                        }
                    });


                    displayBarChart("Absences par Salle", absencesByRoom);
                    String reportContent = generateProfessionalReport("Rapport par Salle", absencesByRoom, null);
                    showReportDialog("Rapport par Salle", reportContent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erreur Firestore : " + e.getMessage(), e);
                });
    }


    private void fetchReportByPeriod(String startDate, String endDate) {
        db.collection("absences")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> absencesByPeriod = new HashMap<>();

                    queryDocumentSnapshots.forEach(document -> {
                        String teacherName = document.getString("teacherName");
                        if (teacherName != null) {
                            absencesByPeriod.put(teacherName, absencesByPeriod.getOrDefault(teacherName, 0) + 1);
                        }
                    });


                    displayBarChart("Absences par Période", absencesByPeriod);
                    String reportContent = generateProfessionalReport("Rapport par Période (" + startDate + " à " + endDate + ")", absencesByPeriod, null);
                    showReportDialog("Rapport par Période", reportContent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erreur Firestore : " + e.getMessage(), e);
                });
    }

    private void displayBarChart(String title, Map<String, Integer> data) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>(data.keySet());
        int index = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, title);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.invalidate();

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        barChart.getDescription().setEnabled(false);
    }


    private String generateProfessionalReport(String title, Map<String, Integer> data, String additionalInfo) {
        StringBuilder reportContent = new StringBuilder();
        reportContent.append(title).append("\n\n");

        int totalAbsences = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            reportContent.append(entry.getKey()).append(" a enregistré ").append(entry.getValue()).append(" absence(s).\n");
            totalAbsences += entry.getValue();
        }

        reportContent.append("\nNombre total d'absences : ").append(totalAbsences).append("\n");
        if (additionalInfo != null) {
            reportContent.append(additionalInfo);
        }

        return reportContent.toString();
    }


    private void showReportDialog(String title, String content) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("Exporter en PDF", (dialog, which) -> exportToPDF(title, content))
                .setNegativeButton("Fermer", null)
                .create()
                .show();
    }


    private void exportToPDF(String title, String content) {
        try {
            File pdfDir = new File(getExternalFilesDir(null), "Reports");
            if (!pdfDir.exists()) {
                pdfDir.mkdirs();
            }

            File pdfFile = new File(pdfDir, title.replace(" ", "_") + ".pdf");

            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph(title).setBold().setFontSize(18));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(content));

            document.close();

            Toast.makeText(this, "Rapport exporté : " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();


            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la génération du PDF : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void showDatePickerDialog(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    listener.onDateSelected(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }


    private interface OnDateSelectedListener {
        void onDateSelected(String date);
    }
}
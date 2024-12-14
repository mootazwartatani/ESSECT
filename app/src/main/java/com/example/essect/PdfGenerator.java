package com.example.essect;

import android.os.Environment;
import android.util.Log;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;

public class PdfGenerator {

    private static final String TAG = "PdfGenerator";

    public static void createPdf(String fileName, String content) {
        try {

            File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Reports");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdirs();
            }

            File pdfFile = new File(pdfFolder, fileName);
            FileOutputStream outputStream = new FileOutputStream(pdfFile);


            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);


            document.add(new Paragraph("Rapport des absences").setBold().setFontSize(18));
            document.add(new Paragraph(content));

            document.close();

            Log.d(TAG, "PDF créé avec succès : " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la génération du PDF : " + e.getMessage(), e);
        }
    }
}

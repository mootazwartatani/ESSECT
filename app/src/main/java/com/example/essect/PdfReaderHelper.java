package com.example.essect;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.InputStream;

public class PdfReaderHelper {

    private static final String TAG = "PdfReaderHelper";

    public static String readPdfFile(Context context, Uri fileUri) {
        StringBuilder content = new StringBuilder();

        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            PdfReader pdfReader = new PdfReader(inputStream);
            PdfDocument pdfDocument = new PdfDocument(pdfReader);


            int numberOfPages = pdfDocument.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                String pageContent = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i));
                content.append(pageContent).append("\n");
            }

            pdfDocument.close();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la lecture du fichier PDF : " + e.getMessage(), e);
        }

        return content.toString();
    }
}

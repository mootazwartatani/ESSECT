package com.example.essect;
import android.os.Environment;
import android.util.Log;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelWriter {
    private static final String TAG = "ExcelWriter";

    public static void writeExcelFile(String fileName, List<Map<String, String>> dataList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Emploi du Temps");


        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Jour");
        headerRow.createCell(1).setCellValue("Heure");
        headerRow.createCell(2).setCellValue("Enseignant");
        headerRow.createCell(3).setCellValue("Classe");


        int rowIndex = 1;
        for (Map<String, String> rowData : dataList) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(rowData.get("day"));
            row.createCell(1).setCellValue(rowData.get("time"));
            row.createCell(2).setCellValue(rowData.get("teacher"));
            row.createCell(3).setCellValue(rowData.get("class"));
        }


        try {
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Schedules");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, fileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            Log.d(TAG, "Fichier Excel créé avec succès : " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la création du fichier Excel : " + e.getMessage(), e);
        }
    }
}

package com.example.essect;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {

    private static final String TAG = "ExcelReader";


    public static List<Map<String, String>> readExcelFile(Context context, Uri fileUri) {
        List<Map<String, String>> scheduleList = new ArrayList<>();

        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {

                    continue;
                }

                Map<String, String> schedule = new HashMap<>();
                schedule.put("day", row.getCell(0).getStringCellValue());
                schedule.put("time", row.getCell(1).getStringCellValue());
                schedule.put("teacher", row.getCell(2).getStringCellValue());
                schedule.put("class", row.getCell(3).getStringCellValue());

                scheduleList.add(schedule);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la lecture du fichier Excel : " + e.getMessage(), e);
        }

        return scheduleList;
    }
}
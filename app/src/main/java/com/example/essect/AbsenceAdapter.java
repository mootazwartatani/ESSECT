package com.example.essect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import android.widget.ArrayAdapter;

public class AbsenceAdapter extends ArrayAdapter<Map<String, String>> {

    public AbsenceAdapter(@NonNull Context context, @NonNull List<Map<String, String>> absences) {
        super(context, 0, absences);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_absence, parent, false);
        }

        Map<String, String> absence = getItem(position);

        TextView teacherNameTextView = convertView.findViewById(R.id.teacherNameTextView);
        TextView dateTextView = convertView.findViewById(R.id.dateTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);

        teacherNameTextView.setText(absence.get("teacherName"));
        dateTextView.setText(absence.get("date"));
        timeTextView.setText(absence.get("time"));

        return convertView;
    }
}
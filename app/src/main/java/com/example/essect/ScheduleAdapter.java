package com.example.essect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> scheduleList;

    public ScheduleAdapter(Context context, List<Map<String, Object>> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Map<String, Object> schedule = scheduleList.get(position);

        holder.dayTextView.setText("Jour : " + (schedule.get("day") != null ? schedule.get("day") : "N/A"));
        holder.timeTextView.setText("Heure : " + (schedule.get("time") != null ? schedule.get("time") : "N/A"));
        holder.teacherTextView.setText("Enseignant : " + (schedule.get("teacher") != null ? schedule.get("teacher") : "N/A"));
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView, timeTextView, teacherTextView;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            teacherTextView = itemView.findViewById(R.id.teacherTextView);
        }
    }
}
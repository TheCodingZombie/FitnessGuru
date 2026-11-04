package com.undead.fitnessguru;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ExerciseViewHolder> {

    public interface Click {
        void open(ExerciseHistory e, View shared);
    }
    private final Context context;
    private final Click click;
    private List<ExerciseHistory> exercises = new ArrayList<>();

    // Constructor accepts the workout list and a workout callback.
    public ExerciseHistoryAdapter(Context c, Click l) { context = c; click = l; }
    public void setExercises(List<ExerciseHistory> list) {
        exercises = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ExerciseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ){
        return new ExerciseViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            ExerciseViewHolder holder, int position
    ){
        ExerciseHistory e = exercises.get(position);
        holder.title.setText(e.name);

        StringBuilder sb = new StringBuilder();

        for (ExerciseHistory.SetHistory sh : e.setHistoryList){
            sb.append(sh.bestWeight + " lbs PB at " + new Date(sh.timeStarted) + ".\n");
        }

        holder.exerciseList.setText(sb.toString());

        holder.expandButton.setOnClickListener(v->click.open(e,holder.itemView));
    }

    @Override
    public int getItemCount(){
        return exercises.size();
    }

    // Inner ViewHolder class:
    // Holds references to the cover ImageView and title TextView.
    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView title, exerciseList;

        ImageButton expandButton;

        ExerciseViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.exerciseTitle);
            expandButton = v.findViewById(R.id.expandButton);
            exerciseList = v.findViewById(R.id.setList);
        }
    }
}
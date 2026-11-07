package com.undead.fitnessguru;

import android.content.Context;
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
import java.util.Locale;

public class PresetWorkoutAdapter extends RecyclerView.Adapter<PresetWorkoutAdapter.PresetHolder> {
    public interface Click {
        void open(Workout w, View shared);
        void delete(Workout w, PresetHolder v);
    }
    private final Context context;
    private final Click click;
    private List<Workout> workouts = new ArrayList<>();

    // Constructor accepts the workout list and a workout callback.
    public PresetWorkoutAdapter(Context c, Click l) { context = c; click = l; }
    public void setWorkouts(List<Workout> list) {
        workouts = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PresetHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ){
        return new PresetHolder(
                LayoutInflater.from(context).inflate(R.layout.item_workout, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            PresetHolder holder, int position
    ){
        Workout w = workouts.get(position);
        holder.title.setText(w.name);

        holder.expandButton.setOnClickListener(v->click.open(w,holder.itemView));
        holder.deleteButton.setOnClickListener(v->click.delete(w,holder));

        StringBuilder string = new StringBuilder();

        for (Exercise e: w.exerciseList){
            string.append(e.exerciseName + "\n");
        }

        holder.exerciseList.setText(string.toString());
    }

    @Override
    public int getItemCount(){
        return workouts.size();
    }

    // Inner ViewHolder class:
    // Holds references to the cover ImageView and title TextView.
    static class PresetHolder extends RecyclerView.ViewHolder {
        TextView title, exerciseList;

        ImageButton expandButton;

        Button deleteButton;

        PresetHolder(View v){
            super(v);
            title = v.findViewById(R.id.workoutTitle);
            expandButton = v.findViewById(R.id.expandButton);
            deleteButton = v.findViewById(R.id.deleteWorkout);
            exerciseList = v.findViewById(R.id.workoutList);
        }
    }
}

package com.undead.fitnessguru;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class WorkoutEditActivity extends FragmentActivity {

    WorkoutDatabase db;
    Workout w;

    private ViewPager2 viewPager;

    private WorkoutPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        w = new Workout();
        db = new WorkoutDatabase(this);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.exercisePager);
        pagerAdapter = new WorkoutPagerAdapter(this);
        pagerAdapter.setExercises(w.exerciseList);

        viewPager.setAdapter(pagerAdapter);

        findViewById(R.id.addExercise).setOnClickListener(v -> {
            w.addExercise(new Exercise("Lorem Ipsum"));
            refresh();
        });

        findViewById(R.id.cancelWorkout).setOnClickListener(v->{
            new AlertDialog.Builder(WorkoutEditActivity.this)
                    .setTitle("Cancel Workout?")
                    .setMessage("This does not save your current workout.")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", (d, w) -> {
                        Intent i = new Intent(this, WorkoutActivity.class);
                        startActivity(i);
                    }
                    ).show();
        });

        findViewById(R.id.endWorkout).setOnClickListener(v->{
            new AlertDialog.Builder(WorkoutEditActivity.this)
                    .setTitle("End Workout?")
                    .setMessage("This will save your workout to the database, with all of the exercises you have currently done.")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", (d, x) -> {
                                saveWorkout();
                                Intent i = new Intent(this, WorkoutActivity.class);
                                startActivity(i);
                            }
                    ).show();
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void refresh() {
        if (pagerAdapter==null) return;

        pagerAdapter.setExercises(w.exerciseList);
        viewPager.setCurrentItem(pagerAdapter.getItemCount() - 1);
    }

    private void saveWorkout() {
        w.name = "Workout"; // this probably will be changeable in the full app
        w.exerciseList = pagerAdapter.exercises;

        db.saveWorkoutToDatabase(w);

        // update all personal bests
        for (Exercise e : w.exerciseList){
            db.updateExerciseInDatabase(e, w.timeStarted);
        }
    }

    public class WorkoutPagerAdapter extends RecyclerView.Adapter<WorkoutPagerAdapter.PageHolder> {
        private final Context context;

        private List<Exercise> exercises = new ArrayList<>();

        public WorkoutPagerAdapter(Context context) {
            this.context = context;
        }

        public void setExercises(List<Exercise> list) {
            exercises = new ArrayList<>(list);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public PageHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_exercise_edit, parent, false);

            return new PageHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull PageHolder holder,
                int position) {
            // Set up the page's data here if necessary.
            holder.exercise = exercises.get(position);
            holder.refreshSets();
        }
        private class PageHolder extends RecyclerView.ViewHolder {
            TextView title, empty;

            RecyclerView setList;

            Exercise exercise;

            ExerciseAdapter adapter;

            public PageHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.exerciseTitle);
                empty = itemView.findViewById(R.id.emptyMessage);
                setList = itemView.findViewById(R.id.setList);
                setList.setLayoutManager(new LinearLayoutManager(WorkoutEditActivity.this, LinearLayoutManager.VERTICAL, false));

                adapter = new ExerciseAdapter(WorkoutEditActivity.this, new ExerciseAdapter.Click() {
                    @Override
                    public void weightChanged(Exercise.Set s, String text) {
                        s.weight = Float.parseFloat(text);
                    }

                    @Override
                    public void repsChanged(Exercise.Set s, String text) {
                        s.reps = Integer.parseInt(text);
                    }

                    @Override
                    public void delete(Exercise.Set s) {
                        exercise.removeSet(s);
                        refreshSets();
                    }

                    @Override
                    public void copy(Exercise.Set s) {
                        exercise.addSet(new Exercise.Set(s));
                        refreshSets();
                    }
                });
                setList.setAdapter(adapter);

                itemView.findViewById(R.id.addSet).setOnClickListener(v->{
                    exercise.addSet(new Exercise.Set());
                    refreshSets();
                });

                itemView.findViewById(R.id.removeSet).setOnClickListener(v->{
                    exercise.popSet();
                    refreshSets();
                });

                itemView.findViewById(R.id.deleteExercise).setOnClickListener(v->{
                    w.exerciseList.remove(exercise);
                    refresh();
                });
            }

            private void refreshSets() {
                adapter.setSets(exercise.sets);
                empty.setVisibility(exercise.sets.isEmpty() ? View.VISIBLE : View.GONE);
            }

        }
    }
}

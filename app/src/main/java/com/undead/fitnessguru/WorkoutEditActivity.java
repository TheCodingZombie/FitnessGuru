package com.undead.fitnessguru;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import java.util.Locale;

public class WorkoutEditActivity extends FragmentActivity {

    WorkoutDatabase db;
    Workout w;

    private ViewPager2 viewPager;

    private WorkoutPagerAdapter pagerAdapter;

    private long seconds = 0;

    private boolean running, wasRunning;

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

        String workout_name = getIntent().getStringExtra("workout_name");
        w.name = workout_name != null ? workout_name : "New Workout";

        ((TextView)findViewById(R.id.heading)).setText(w.name);

        db = new WorkoutDatabase(this);

        if (savedInstanceState != null) {

            // Get the previous state of the timer
            // if the activity has been
            // destroyed and recreated.
            seconds
                    = savedInstanceState
                    .getLong("seconds");
            running
                    = savedInstanceState
                    .getBoolean("running");
            wasRunning
                    = savedInstanceState
                    .getBoolean("wasRunning");
        }
        running = true;
        runTimer();

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.exercisePager);
        pagerAdapter = new WorkoutPagerAdapter(this);
        pagerAdapter.setExercises(w.exerciseList);

        viewPager.setAdapter(pagerAdapter);

        findViewById(R.id.addExercise).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutEditActivity.this);
            builder.setTitle("Enter Exercise Name");

            // Set up the workout name input
            EditText name = new EditText(this);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            name.setHint("Exercise Name...");

            builder.setView(name);

            // Set up the buttons
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                String newName = name.getText().toString().trim();
                newName = newName.isEmpty() ? "New Exercise" : newName;

                w.addExercise(new Exercise(newName));
                refresh();
            });
            builder.setNegativeButton("Cancel", null);

            builder.show();
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
    public void onSaveInstanceState(
            Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState
                .putLong("seconds", seconds);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
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

    @Override
    protected void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    protected void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    private void refresh() {
        if (pagerAdapter==null) return;

        pagerAdapter.setExercises(w.exerciseList);
        viewPager.setCurrentItem(pagerAdapter.getItemCount() - 1);
    }

    private void saveWorkout() {
        w.totalTime = seconds; // how long the workout took.

        w.exerciseList = pagerAdapter.exercises;

        db.saveWorkoutToDatabase(w);

        // update all personal bests
        for (Exercise e : w.exerciseList){
            db.updateExerciseInDatabase(e, w.timeStarted);
        }
    }

    private void runTimer()
    {
        final TextView timeView = (TextView)findViewById(R.id.timer);

        final Handler handler = new Handler();

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time = String.format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                timeView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
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

            holder.title.setText(holder.exercise.exerciseName);

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

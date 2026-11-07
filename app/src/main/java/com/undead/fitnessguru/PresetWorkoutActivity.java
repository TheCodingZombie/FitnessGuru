package com.undead.fitnessguru;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PresetWorkoutActivity extends AppCompatActivity {

    WorkoutDatabase db;

    PresetWorkoutAdapter adapter;

    EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preset_workouts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new WorkoutDatabase(this);
        search = findViewById(R.id.searchInput);

        RecyclerView list = findViewById(R.id.workoutList);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new PresetWorkoutAdapter(this, new PresetWorkoutAdapter.Click() {
            @Override
            public void open(Workout w, View shared) {
                View listView = shared.findViewById(R.id.workoutListView);

                listView.setVisibility(listView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );

                ImageButton expandButton = shared.findViewById(R.id.expandButton);

                expandButton.setRotation(listView.getVisibility() == View.VISIBLE ? 0 : 180 );
            }

            @Override
            public void delete(Workout w, PresetWorkoutAdapter.PresetHolder v) {
                new AlertDialog.Builder(PresetWorkoutActivity.this)
                        .setTitle("Delete preset??")
                        .setMessage("This permanently deletes " + w.name + ".")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, x) -> {
                                    db.deletePreset(w.id);
                                    refresh();
                                }
                        ).show();
            }
        });
        list.setAdapter(adapter);

        search.addTextChangedListener(
                new TextWatcher(){
                    public void beforeTextChanged(CharSequence s,int st,int c,int a){}
                    public void onTextChanged(CharSequence s,int st,int before,int count){
                        refresh();
                    }
                    public void afterTextChanged(Editable e){}
                }
        );

        findViewById(R.id.clearSearch).setOnClickListener(v->search.setText(""));

        findViewById(R.id.newWorkout).setOnClickListener(v-> {
            Workout testPreset = new Workout();
            testPreset.name = "Preset New";

            testPreset.addExercise(new Exercise("Lunges"));
            testPreset.addExercise(new Exercise("Chest Flies"));

            db.savePresetToDatabase(testPreset);
            refresh();
        });

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString("activity_name", this.getClass().getSimpleName());

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.bottomBar, BottomNavFragment.class, bundle)
                    .commit();
        }
    }

    @Override protected void onResume(){
        super.onResume();
        if (db!=null) refresh();
    }

    void refresh(){
        if (adapter==null) return;

        adapter.setWorkouts(db.searchPresets(search.getText().toString().trim()));
        findViewById(R.id.emptyMessage).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
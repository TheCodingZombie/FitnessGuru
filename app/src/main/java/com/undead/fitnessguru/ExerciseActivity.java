package com.undead.fitnessguru;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ExerciseActivity extends AppCompatActivity {

    WorkoutDatabase db;

    ExerciseHistoryAdapter adapter;

    EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercise_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new WorkoutDatabase(this);
        search = findViewById(R.id.searchInput);

        RecyclerView list = findViewById(R.id.exerciseList);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new ExerciseHistoryAdapter(this, (e, shared) -> {
            View listView = shared.findViewById(R.id.exerciseListView);

            listView.setVisibility(listView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );

            ImageButton expandButton = shared.findViewById(R.id.expandButton);

            expandButton.setRotation(listView.getVisibility() == View.VISIBLE ? 0 : 180 );


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

        adapter.setExercises(db.getAllExercises());
        findViewById(R.id.emptyMessage).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
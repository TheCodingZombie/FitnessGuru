package com.undead.fitnessguru;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MeasurementActivity extends AppCompatActivity {

    WorkoutDatabase db;

    MeasurementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_measurement_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new WorkoutDatabase(this);

        RecyclerView list = findViewById(R.id.measurementList);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new MeasurementAdapter(this);
        list.setAdapter(adapter);

        findViewById(R.id.goToWorkoutHistory).setOnClickListener(v-> {
            startActivity(new Intent(this, WorkoutActivity.class));
        });

        findViewById(R.id.goToExerciseHistory).setOnClickListener(v-> {
            startActivity(new Intent(this, ExerciseActivity.class));
        });

        findViewById(R.id.addBodyPart).setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(MeasurementActivity.this);
            builder.setTitle("New Measurement");

            EditText measurement = new EditText(this);
            measurement.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
            measurement.setHint("Number...");

            EditText body_part = new EditText(this);
            body_part.setInputType(InputType.TYPE_CLASS_TEXT);
            body_part.setHint("Enter Body Part (e.g. Bicep, Tricep, Thigh)...");

            Spinner units = new Spinner(this);
            String[] options={"in", "cm"};
            units.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));
            units.setSelection(0);

            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.VERTICAL);

            l.addView(body_part);
            l.addView(measurement);
            l.addView(units);

            builder.setView(l);

            // Set up the buttons
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                if (body_part.getText().toString().trim().isEmpty() || measurement.getText().toString().trim().isEmpty()){
                    Toast.makeText(MeasurementActivity.this, "Fill out the form first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Measurement m = new Measurement(
                    body_part.getText().toString().trim(),
                    units.getSelectedItem().toString().trim(),
                    Double.valueOf(measurement.getText().toString().trim()),
                    Calendar.getInstance().getTimeInMillis()
                );

                db.saveMeasurementToDatabase(m);
                refresh();
                Toast.makeText(MeasurementActivity.this, "Measurement successfully added.", Toast.LENGTH_LONG).show();
                dialog.cancel();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

    }

    @Override protected void onResume(){
        super.onResume();
        if (db!=null) refresh();
    }

    void refresh(){
        if (adapter==null) return;

        List<String> stringList = db.getAllBodyParts();

        Log.d("stringList", stringList.toString());

        List<List<Measurement>> ml = new ArrayList<>();
        for (String bp : stringList){
            ml.add(db.getAllMeasurementsOfBodyPart(bp));
            Log.d("bodyParts", db.getAllMeasurementsOfBodyPart(bp).toString());

        }

        adapter.setMeasurements(ml);
        findViewById(R.id.emptyMessage).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
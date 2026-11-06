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

    private final List<String> bodyParts = Arrays.asList(
            "Weight",
            "Left Arm", "Left Forearm",
            "Right Arm", "Right Forearm",
            "Shoulders", "Chest", "Neck",
            "Waist", "Hips",
            "Glutes", "Left Thigh", "Left Calf",
            "Right Thigh", "Right Calf");

    private List<String> bodyCopy = new ArrayList<>();

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

        adapter = new MeasurementAdapter(this, shared -> {
            View listView = shared.findViewById(R.id.measurementListView);

            listView.setVisibility(listView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );

            ImageButton expandButton = shared.findViewById(R.id.expandButton);

            expandButton.setRotation(listView.getVisibility() == View.VISIBLE ? 0 : 180 );
        });
        list.setAdapter(adapter);

        adapter.setBodyParts(bodyParts);

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

            Spinner body_part = new Spinner(this);
            String[] parts= bodyParts.toArray(new String[0]);
            body_part.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,parts));
            body_part.setSelection(0);

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
                if (body_part.getSelectedItem().toString().trim().isEmpty() || measurement.getText().toString().trim().isEmpty()){
                    Toast.makeText(MeasurementActivity.this, "Fill out the form first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Measurement m = new Measurement(
                    body_part.getSelectedItem().toString().trim(),
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

        findViewById(R.id.startFullBody).setOnClickListener(v->{
            createBodyweightMeasurement(true);
        });

        findViewById(R.id.doBodyWeight).setOnClickListener(v->{
            createBodyweightMeasurement(false);
        });
    }

    @Override protected void onResume(){
        super.onResume();
        if (db!=null) refresh();
    }

    void refresh(){
        if (adapter==null) return;
        List<List<Measurement>> ml = new ArrayList<>();
        for (String bp : bodyParts){
            ml.add(db.getAllMeasurementsOfBodyPart(bp));

        }
        adapter.setMeasurements(ml);
        findViewById(R.id.emptyMessage).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // if part of the full body measurement, continue to the rest of the body parts.
    void createBodyweightMeasurement(boolean partOfFullBody){
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasurementActivity.this);
        builder.setTitle("New Weight Measurement");

        EditText measurement = new EditText(this);
        measurement.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        measurement.setHint("Number...");

        Spinner units = new Spinner(this);
        String[] options={"lb", "kg"};
        units.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));
        units.setSelection(0);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);

        l.addView(measurement);
        l.addView(units);

        builder.setView(l);

        // Set up the buttons
        builder.setPositiveButton((partOfFullBody ? "Next" : "Confirm"), (dialog, which) -> {
            if (measurement.getText().toString().trim().isEmpty()){
                Toast.makeText(MeasurementActivity.this, "Fill out the form first!", Toast.LENGTH_SHORT).show();
                return;
            }

            Measurement m = new Measurement(
                    "Weight",
                    units.getSelectedItem().toString().trim(),
                    Double.valueOf(measurement.getText().toString().trim()),
                    Calendar.getInstance().getTimeInMillis()
            );

            db.saveMeasurementToDatabase(m);



            if (partOfFullBody) {
                // create a copy so that the circuit can iterate through it.
                bodyCopy = new ArrayList<>(bodyParts);

                bodyCopy.removeFirst(); // remove weight.

                createFullBodyMeasurement(bodyCopy.getFirst());
            }
            else {
                refresh();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(partOfFullBody ? "Skip" : "Cancel", (dialog, which) -> {
            if (partOfFullBody) {
                // create a copy so that the circuit can iterate through it.
                bodyCopy = new ArrayList<>(bodyParts);

                bodyCopy.removeFirst(); // remove weight.

                createFullBodyMeasurement(bodyCopy.getFirst());
            }
            else {
                refresh();
                dialog.cancel();
            }
        });

        builder.show();
    }

    void createFullBodyMeasurement(String bodyPart) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeasurementActivity.this);
        builder.setTitle("New Measurement for " + bodyPart);

        EditText measurement = new EditText(this);
        measurement.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        measurement.setHint("Number...");

        Spinner units = new Spinner(this);
        String[] options={"in", "cm"};
        units.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));
        units.setSelection(0);

        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);

        l.addView(measurement);
        l.addView(units);

        builder.setView(l);

        // Set up the buttons
        builder.setPositiveButton("Next", (dialog, which) -> {
            if (measurement.getText().toString().trim().isEmpty()){
                Toast.makeText(MeasurementActivity.this, "Fill out the form first!", Toast.LENGTH_SHORT).show();
                return;
            }

            Measurement m = new Measurement(
                    bodyPart,
                    units.getSelectedItem().toString().trim(),
                    Double.valueOf(measurement.getText().toString().trim()),
                    Calendar.getInstance().getTimeInMillis()
            );

            db.saveMeasurementToDatabase(m);

            // Then we iterate through the list until it is empty.
            bodyCopy.removeFirst();
            if (!bodyCopy.isEmpty()){
                createFullBodyMeasurement(bodyCopy.getFirst());
            }
            else {
                // finished the list.
                Toast.makeText(MeasurementActivity.this, "Successfully finished a full body measurement.", Toast.LENGTH_LONG).show();
                refresh();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Skip", (dialog, which) -> {
            bodyCopy.removeFirst();
            if (!bodyCopy.isEmpty()){
                createFullBodyMeasurement(bodyCopy.getFirst());
            }
            else {
                // finished the list.
                Toast.makeText(MeasurementActivity.this, "Successfully finished a full body measurement.", Toast.LENGTH_LONG).show();
                refresh();
                dialog.cancel();
            }
        });

        builder.show();
    }

}
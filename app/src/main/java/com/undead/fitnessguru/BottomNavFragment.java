package com.undead.fitnessguru;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.Button;

public class BottomNavFragment extends Fragment {

    Button workoutHistory, exerciseHistory, measurementHistory, presetWorkouts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_bottom_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        workoutHistory = view.findViewById(R.id.goToWorkoutHistory);
        exerciseHistory = view.findViewById(R.id.goToExerciseHistory);
        measurementHistory = view.findViewById(R.id.goToMeasurements);
        presetWorkouts = view.findViewById(R.id.goToPresets);

        updateCurrentActivity(requireArguments().getString("activity_name"));

        workoutHistory.setOnClickListener(v -> startActivity(new Intent(view.getContext(),WorkoutActivity.class)));
        exerciseHistory.setOnClickListener(v -> startActivity(new Intent(view.getContext(),ExerciseActivity.class)));
        measurementHistory.setOnClickListener(v -> startActivity(new Intent(view.getContext(),MeasurementActivity.class)));
        presetWorkouts.setOnClickListener(v -> startActivity(new Intent(view.getContext(),PresetWorkoutActivity.class)));
    }

    public void updateCurrentActivity(String newActivity) {
        switch (newActivity){
            case "WorkoutActivity": workoutHistory.setVisibility(View.GONE); break;
            case "ExerciseActivity": exerciseHistory.setVisibility(View.GONE); break;
            case "MeasurementActivity": measurementHistory.setVisibility(View.GONE); break;
            case "PresetWorkoutActivity": presetWorkouts.setVisibility(View.GONE); break;
        }
    }
}
package com.undead.fitnessguru;

import androidx.fragment.app.Fragment;

import android.os.*;
import android.view.*;

public class ExerciseFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(
                R.layout.fragment_exercise_edit, container, false);
    }
}
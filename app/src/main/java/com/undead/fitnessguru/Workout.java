package com.undead.fitnessguru;

import android.content.ContentValues;

import org.json.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Workouts are collections of exercises. They also have times attached to them.
public class Workout {

    // timeStarted stores the unix time code of when the workout was started
    // totalTime stores the seconds the workout takes
    public long timeStarted, totalTime, id;

    public String name;
    public List<Exercise> exerciseList = new ArrayList<>();

    public Workout() {
        Calendar now = Calendar.getInstance();

        timeStarted = now.getTimeInMillis() * 1000L;
    }

    public Workout(List<Exercise> exercises){
        Calendar now = Calendar.getInstance();

        timeStarted = now.getTimeInMillis() * 1000L;

        exerciseList = exercises;
    }

    public Workout(long ts){
        Calendar now = Calendar.getInstance();

        timeStarted = ts;
    }

    public void addExercise(Exercise exercise){
        exerciseList.add(exercise);
    }

    public void deleteExercise(long exerciseId){
        for (Exercise e : exerciseList){
            if (e.id == exerciseId){
                exerciseList.remove(e);
                return;
            }
        }
    }

    // get the values from the workout
    public ContentValues values() {
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("timeStarted", timeStarted);
        values.put("totalTime", totalTime);

        JSONArray exercises = new JSONArray();
        for (Exercise e: exerciseList) {
            exercises.put(e.detailsToJSON());
        }
        values.put("exerciseList", exercises.toString());

        return values;
    }
}

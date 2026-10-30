package com.undead.fitnessguru;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "workouts.db";
    private static final int DB_VERSION = 2;

    public WorkoutDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE workouts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "timeStarted INTEGER DEFAULT 0, " +
                        "totalTime INTEGER DEFAULT 0, " +
                        "exerciseList TEXT)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2 && oldVersion == 1){
            db.execSQL(
                    "ALTER TABLE workouts " +
                            "ADD name TEXT;"
            );
        }
    }

    public long saveWorkoutToDatabase (Workout w){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = w.values();

        if (w.id > 0){
            int updated = db.update(
                    "workouts",
                    values,
                    "id=?",
                    new String[]{String.valueOf(w.id)}
            );

            if (updated > 0) {
                return w.id;
            }
        }

        long id = db.insert("workouts", null, values);
        if (id > 0) {
            w.id = id;
        }
        return id;
    }

    private void jsonToList (JSONArray list, List<Exercise> output) {
        if (list == null) return;

        for (int i = 0; i < list.length(); i++){
            Object item = list.opt(i);

            if (item != null && item != JSONObject.NULL){
                Exercise res = new Exercise();
                JSONObject e = (JSONObject) item;

                res.exerciseName = e.optString("name");

                JSONArray sets = e.optJSONArray("sets");
                try {
                    for (int j = 0; j < sets.length(); j++) {
                        Exercise.Set s = new Exercise.Set();
                        JSONObject o = sets.optJSONObject(j);

                        s.weight = (float) o.optDouble("weight", 0.0);
                        s.reps = o.optInt("reps", 0);
                        s.unit = o.optString("unit", "lbs");

                        res.addSet(s);
                    }
                }
                catch (Exception ignored){

                }

                output.add(res);
            }
        }

    }

    private Workout read(Cursor cursor) {
        long timeStarted = cursor.getLong(
                cursor.getColumnIndexOrThrow("timeStarted")
        );

        Workout w = new Workout(timeStarted);
        w.id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id"));
        w.name = cursor.getString(
                cursor.getColumnIndexOrThrow("name"));
        w.totalTime = cursor.getLong(
                cursor.getColumnIndexOrThrow("totalTime"));

        String json = cursor.getString(
                cursor.getColumnIndexOrThrow("exerciseList"));

        if (json != null && !json.isEmpty()) {
            try {
                JSONArray eList = new JSONArray(json);

                jsonToList(eList, w.exerciseList);
            } catch (Exception e) {
                Log.e("WorkoutDatabase", "Could not parse details for workout " + w.id);
            }
        }

        return w;
    }

    public Workout get(long id) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM workouts WHERE _id=?",
                new String[]{String.valueOf(id)})) {

            return cursor.moveToFirst() ? read(cursor) : null;
        }
    }

    public ArrayList<Workout> search(String query) {
        ArrayList<Workout> results = new ArrayList<>();

        String search = query == null
                ? ""
                : query.toLowerCase(Locale.ROOT);

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM workouts " +
                        "WHERE lower(name) LIKE ? " +
                        "ORDER BY name COLLATE NOCASE",
                new String[]{"%" + search + "%"})) {

            while (cursor.moveToNext()) {
                results.add(read(cursor));
            }
        }

        return results;
    }
}

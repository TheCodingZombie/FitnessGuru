package com.undead.fitnessguru;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class WorkoutDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "workouts.db";
    private static final int DB_VERSION = 5;

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

        db.execSQL(
                "CREATE TABLE exercises (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "details_json TEXT )"
        );

        db.execSQL(
                "CREATE TABLE measurements (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "bodyPart TEXT, " +
                        "unit TEXT, " +
                        "measurement DOUBLE, " +
                        "timeTaken INTEGER)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion >= 2 && oldVersion == 1){
            db.execSQL(
                    "ALTER TABLE workouts " +
                            "ADD name TEXT;"
            );
        }

        if (newVersion >= 3 && oldVersion <= 2) {
            db.execSQL(
                "CREATE TABLE exercises (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "personalBestWeight INTEGER DEFAULT 0, " +
                "personalBestReps INTEGER DEFAULT 0)"
            );
        }

        if (newVersion >= 4 && oldVersion == 3) {
            db.execSQL(
                    "ALTER TABLE exercises " +
                            "DROP COLUMN personalBestWeight;"
            );

            db.execSQL(
                    "ALTER TABLE exercises " +
                            "DROP COLUMN personalBestReps;"
            );

            db.execSQL(
                    "ALTER TABLE exercises " +
                            "ADD details_json TEXT;"
            );
        }

        if (newVersion >= 5 && oldVersion <= 4){
            db.execSQL(
                    "CREATE TABLE measurements (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "bodyPart TEXT, " +
                            "unit TEXT, " +
                            "measurement DOUBLE, " +
                            "timeTaken INTEGER)"
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
                Log.d("Database", "Successfully updated workout of id " + updated + ".");
                return w.id;
            }
        }

        long id = db.insert("workouts", null, values);
        if (id > 0) {
            w.id = id;
        }

        Log.d("Database", "Successfully stored workout of id " + id + ".");
        return id;
    }

    public long updateExerciseInDatabase (Exercise e, long timeStarted) {
        ExerciseHistory eh = null;

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM exercises WHERE id=?",
                new String[]{String.valueOf(e.id)})) {


            eh = cursor.moveToFirst() ? readExerciseHistory(cursor) : null;
        }
        catch (Exception ignored) {

        }

        if (eh == null){
            eh = getFirstExerciseOfName(e.exerciseName);
        }

        SQLiteDatabase db = getWritableDatabase();

        if (eh == null){
            // the check failed, assume we don't have anything in the database
            // and add this exercise to the database instead
            eh = new ExerciseHistory(e, timeStarted);

            long id = db.insert("exercises", null, eh.values());
            if (id > 0) {
                eh.id = id;
                e.id = id;
            }

            Log.d("Database", "Successfully stored exercise of id " + id + ".");
            return id;
        }
        else {
            e.id = eh.id;

            eh.addNewExerciseToHistory(e, timeStarted);
            Log.d("ExerciseHistory", eh.listToJSON().toString());

            int updated = db.update(
                    "exercises",
                    eh.values(),
                    "id=?",
                    new String[]{String.valueOf(eh.id)}
            );

            if (updated > 0) {
                Log.d("Database", "Successfully updated exercise of id " + updated + ".");
                return eh.id;
            }

            // we didn't change the exercise, as it wasn't found.
            return -1;
        }
    }

    public long saveMeasurementToDatabase (Measurement m) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = m.values();

        if (m.id > 0){
            int updated = db.update(
                    "measurements",
                    values,
                    "id=?",
                    new String[]{String.valueOf(m.id)}
            );

            if (updated > 0) {
                Log.d("Database", "Successfully updated measurement of id " + updated + ".");
                return m.id;
            }
        }

        long id = db.insert("measurements", null, values);
        if (id > 0) {
            m.id = id;
        }

        Log.d("Database", "Successfully stored measurement of id " + id + ".");
        return id;
    }

    private void jsonToList (JSONArray list, List<Exercise> output) {
        if (list == null) return;


        for (int i = 0; i < list.length(); i++){
            Object item = list.opt(i);

            if (item != null && item != JSONObject.NULL){
                Exercise res = new Exercise();
                try {
                    JSONObject e = new JSONObject(item.toString());

                    res.exerciseName = e.optString("name");

                    JSONArray sets = e.optJSONArray("sets");

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
                Log.e("WorkoutDatabase", "Could not parse details for workout " + w.id + "\n with the following error: " + e.toString());
            }
        }

        return w;
    }

    private ExerciseHistory readExerciseHistory(Cursor cursor) {
        ExerciseHistory eh = new ExerciseHistory();

        eh.id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id"));
        eh.name = cursor.getString(
                cursor.getColumnIndexOrThrow("name"));

        String json = cursor.getString(
                cursor.getColumnIndexOrThrow("details_json"));
        eh.setSetHistoryList(json);

        return eh;
    }

    private Measurement readMeasurement(Cursor cursor) {
        Measurement mh = new Measurement();

        mh.id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id"));
        mh.bodyPart = cursor.getString(
                cursor.getColumnIndexOrThrow("bodyPart"));
        mh.unit = cursor.getString(
                cursor.getColumnIndexOrThrow("unit"));
        mh.quantity = cursor.getDouble(
                cursor.getColumnIndexOrThrow("measurement"));
        mh.timeTaken = cursor.getLong(
                cursor.getColumnIndexOrThrow("timeTaken"));

        return mh;

    }

    public Workout get(long id) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM workouts WHERE id=?",
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

    public ExerciseHistory getFirstExerciseOfName(String query) {
        String search = query == null
                ? ""
                : query.toLowerCase(Locale.ROOT);

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM exercises " +
                        "WHERE lower(name) LIKE ? " +
                        "ORDER BY name COLLATE NOCASE",
                new String[]{"%" + search + "%"})) {

            return cursor.moveToFirst() ? readExerciseHistory(cursor) : null;
        }
        catch (Exception ignored) {

        }

        return null;
    }

    public ArrayList<Workout> getAll() {
        ArrayList<Workout> results = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM workouts", new String[]{} )) {

            while(cursor.moveToNext()){
                results.add(read(cursor));
            }
        }

        results.sort((o1, o2) -> Long.compare(o2.timeStarted, o1.timeStarted));

        return results;
    }

    public ArrayList<ExerciseHistory> getAllExercises() {
        ArrayList<ExerciseHistory> results = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM exercises", new String[]{} )) {

            while(cursor.moveToNext()){
                results.add(readExerciseHistory(cursor));
            }
        }

        return results;
    }

    public List<Measurement> getAllMeasurementsOfBodyPart(String bodyPart) {
        List<Measurement> list = new ArrayList<>();

        String search = bodyPart == null
                ? ""
                : bodyPart.toLowerCase(Locale.ROOT);

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM measurements " +
                        "WHERE lower(bodyPart) LIKE ? " +
                        "ORDER BY bodyPart COLLATE NOCASE",
                new String[]{"%" + search + "%"})) {

            while(cursor.moveToNext()){
                list.add(readMeasurement(cursor));
            }
        }
        catch (Exception ignored) {

        }

        return list;
    }

    public List<String> getAllBodyParts() {
        List<String> list = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM measurements",
                new String[]{})) {

            while(cursor.moveToNext()){
                list.add(cursor.getString(
                        cursor.getColumnIndexOrThrow("bodyPart")));
            }
        }
        catch (Exception ignored) {

        }

        return list;
    }


    // delete a workout from the database
    public void delete(long id) {
        getWritableDatabase().delete(
                "workouts",
                "id=?",
                new String[]{ String.valueOf(id) }
        );
    }
}

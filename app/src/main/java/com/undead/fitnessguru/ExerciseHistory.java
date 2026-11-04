package com.undead.fitnessguru;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// Used to represent an exercise's history
public class ExerciseHistory {

    public long id;

    public String name;

    public static class SetHistory {
        public long timeStarted;
        public float bestWeight;

        public SetHistory(List<Exercise.Set> list){
            this.timeStarted = Calendar.getInstance().getTimeInMillis() / 1000L;

            this.setBestWeight(list);
        }

        public SetHistory(List<Exercise.Set> list, long timeStarted){
            this.timeStarted = timeStarted;

            this.setBestWeight(list);
        }

        public SetHistory(JSONObject j){
            try {
                timeStarted = j.optLong("timeStarted");
                bestWeight = (float) j.optDouble("bestWeight");
            }
            catch (Exception ignored){}
        }

        public JSONObject toJson() {
            JSONObject j = new JSONObject();
            try {
                j.put("timeStarted", timeStarted);
                j.put("bestWeight", bestWeight);
            }
            catch (Exception ignored){}

            return j;
        }

        public void setBestWeight(List<Exercise.Set> list){
            float res = -999;

            for (Exercise.Set e : list){
                if (e.weight > res) res = e.weight;
            }

            bestWeight = res;
        }
    }

    public List<SetHistory> setHistoryList = new ArrayList<>();

    public ExerciseHistory() {}

    public ExerciseHistory(String name){
        this.name = name;
    }

    public ExerciseHistory(Exercise e){
        this.name = e.exerciseName;

        SetHistory sh = new SetHistory(e.sets);

        this.setHistoryList.add(sh);
    }

    public ExerciseHistory(Exercise e, long timeStarted){
        this.name = e.exerciseName;

        SetHistory sh = new SetHistory(e.sets, timeStarted);

        this.setHistoryList.add(sh);
    }

    // convert the details_json array into a list
    public void setSetHistoryList(String json){
        List<SetHistory> res = new ArrayList<>();

        try{
            JSONArray ja = new JSONArray(json);

            for (int i = 0; i < ja.length(); i++) {
                JSONObject j = ja.optJSONObject(i);

                SetHistory sh = new SetHistory(j);

                res.add(sh);
            }

            setHistoryList = res;
        }
        catch (Exception e) {
            Log.e("JSON Error", "Error decoding json array while making a SetHistory list.");
        }
    }

    // When saving a new workout, this extracts the list of sets done for this exercise.
    public void addNewExerciseToHistory(Exercise e, long timeStarted){
        if (e.id != id) return;

        SetHistory sh = new SetHistory(e.sets);
        sh.timeStarted = timeStarted;

        setHistoryList.add(sh);
    }

    public JSONArray listToJSON() {
        JSONArray ja = new JSONArray();

        for (SetHistory sh : setHistoryList) {
            ja.put(sh.toJson());
        }

        return ja;
    }

    public ContentValues values(){
        ContentValues values = new ContentValues();

        values.put("details_json", listToJSON().toString());
        values.put("name", name);

        return values;
    }


}

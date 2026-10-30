package com.undead.fitnessguru;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// A representation of a singular exercise.
public class Exercise {

    public String exerciseName;

    public long id;

    // sets store the weight used and the reps performed
    public static class Set {
        public float weight;

        // usually lbs, but may include other units later
        public String unit;

        public int reps;

        public Set() {}

        public Set(float w, int r){
            weight = w;
            unit = "lbs";
            reps = r;
        }
    }

    public List<Set> sets = new ArrayList<>();

    public void addSet(float weight, int reps) {
        sets.add(new Set(weight, reps));
    }

    public void addSet(Set s) {
        sets.add(s);
    }


    public void modifySet(int index, Float weight, Integer reps) {
        Set set = sets.get(index);
        if (weight != null) {
            set.weight = weight;
        }
        if (reps != null) {
            set.reps = reps;
        }
    }

    public void popSet() {
        sets.removeLast();
    }

    public void removeSet(int index){
        sets.remove(index);
    }

    // convert this instance's details to a JSON String
    public String detailsToJSON() {
        JSONObject details = new JSONObject();

        try{
            JSONArray setJson = new JSONArray();
            for (Set set : sets){
                JSONObject s = new JSONObject();
                s.put("weight", set.weight);
                s.put("reps", set.reps);
                s.put("unit", set.unit);
                setJson.put(s);
            }

            details.put("name", exerciseName);
            details.put("sets", setJson);
        }
        catch (Exception ignored){
        }

        return details.toString();
    }

}

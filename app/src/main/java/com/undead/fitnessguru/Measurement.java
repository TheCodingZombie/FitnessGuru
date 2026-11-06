package com.undead.fitnessguru;

import android.content.ContentValues;

import java.util.Arrays;
import java.util.Calendar;

public class Measurement {

    long id, timeTaken;
    String bodyPart, unit;

    // the actual measurement part of the measurement
    double quantity;

    String[] units = {"in", "cm", "lb", "kg"};

    public Measurement() {
        unit = "in";

        timeTaken = Calendar.getInstance().getTimeInMillis();
    }

    public Measurement (String bp, String un, double qu, long tt){
        bodyPart = bp;
        quantity = qu;

        unit = Arrays.stream(units).anyMatch(un::equalsIgnoreCase) ? un : "in";
        timeTaken = tt;
    }

    public void quantityToInches() {
        if (!unit.equals("cm")) return;

        quantity *= 0.39370079; // 1 cm = 0.39370079 in
        unit = "in";
    }

    public void quantityToCentimeters() {
        if (!unit.equals("in")) return;

        quantity /= 0.39370079; // 1 cm = 0.39370079 in
        unit = "cm";
    }

    public ContentValues values() {
        ContentValues values = new ContentValues();

        values.put("timeTaken", timeTaken);
        values.put("unit", unit);
        values.put("bodyPart", bodyPart);
        values.put("measurement", quantity);

        return values;
    }
}

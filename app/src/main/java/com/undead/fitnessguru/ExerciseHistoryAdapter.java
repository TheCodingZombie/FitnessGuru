package com.undead.fitnessguru;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciseHistoryAdapter extends RecyclerView.Adapter<ExerciseHistoryAdapter.ExerciseViewHolder> {

    public interface Click {
        void open(ExerciseHistory e, View shared);
    }
    private final Context context;
    private final Click click;
    private List<ExerciseHistory> exercises = new ArrayList<>();

    // Constructor accepts the workout list and a workout callback.
    public ExerciseHistoryAdapter(Context c, Click l) { context = c; click = l; }
    public void setExercises(List<ExerciseHistory> list) {
        exercises = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ExerciseViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ){
        return new ExerciseViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            ExerciseViewHolder holder, int position
    ){
        ExerciseHistory e = exercises.get(position);
        holder.title.setText(e.name);

        //StringBuilder sb = new StringBuilder();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        for (ExerciseHistory.SetHistory sh : e.setHistoryList){
            //sb.append(sh.bestWeight + " lbs PB at " + new Date(sh.timeStarted / 1000L) + ".\n");
            series.appendData(new DataPoint(new Date(sh.timeStarted / 1000L), (double) sh.bestWeight), true, 99);
        }

        //holder.exerciseList.setText(sb.toString());

        holder.pbDisplay.setText("Best PB: " + e.getBestSet() + " lbs");

        holder.graphView.setTitle("PB History");

        holder.graphView.setTitleTextSize(40);

        holder.graphView.addSeries(series);

        series.setDrawBackground(true);
        series.setDrawDataPoints(true);

        holder.graphView.getGridLabelRenderer().setHorizontalLabelsAngle(90);
        holder.graphView.getGridLabelRenderer().setNumHorizontalLabels(e.setHistoryList.size());
        holder.graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));

        holder.graphView.getViewport().setXAxisBoundsManual(true);
        holder.graphView.getViewport().setMinX(e.setHistoryList.getFirst().timeStarted / 1000L);
        holder.graphView.getViewport().setMinY(0);
        holder.graphView.getViewport().setMaxX(e.setHistoryList.getLast().timeStarted / 1000L);
        holder.graphView.getViewport().setMaxY(e.getBestSet());
        holder.graphView.getGridLabelRenderer().setHumanRounding(false);

        holder.expandButton.setOnClickListener(v->click.open(e,holder.itemView));
    }

    @Override
    public int getItemCount(){
        return exercises.size();
    }

    // Inner ViewHolder class:
    // Holds references to the cover ImageView and title TextView.
    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView title, pbDisplay;

        ImageButton expandButton;

        GraphView graphView;

        ExerciseViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.exerciseTitle);
            expandButton = v.findViewById(R.id.expandButton);
            pbDisplay = v.findViewById(R.id.pbDisplay);
            graphView = v.findViewById(R.id.setGraph);
        }
    }
}
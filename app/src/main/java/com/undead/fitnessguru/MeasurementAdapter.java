package com.undead.fitnessguru;

import android.content.Context;
import android.icu.util.Measure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder> {
    private final Context context;
    private List<List<Measurement>> measurements = new ArrayList<>();

    // Constructor accepts the workout list and a workout callback.
    public MeasurementAdapter(Context c) { context = c; }
    public void setMeasurements(List<List<Measurement>> ml) {
        measurements = new ArrayList<>(ml);

        Log.d("measurements", measurements.toString());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeasurementViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ){
        return new MeasurementViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_measurement, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            MeasurementViewHolder holder, int position
    ){
        List<Measurement> m = measurements.get(position);
        Log.d("Measurement", m.getFirst().bodyPart);

        holder.measurements = m;
        holder.bodyPart = holder.measurements.getFirst().bodyPart;

        holder.title.setText(holder.bodyPart);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        for (Measurement M : holder.measurements){
            series.appendData(new DataPoint(new Date(M.timeTaken / 1000L), M.quantity), true, 99);
        }

        //holder.exerciseList.setText(sb.toString());

        holder.pbDisplay.setText("Current Measurement: " + holder.measurements.getFirst().quantity + " " + holder.measurements.getFirst().unit);

        holder.graphView.setTitle("Body Part History");

        holder.graphView.setTitleTextSize(40);

        holder.graphView.addSeries(series);

        series.setDrawBackground(true);
        series.setDrawDataPoints(true);

        holder.graphView.getGridLabelRenderer().setHorizontalLabelsAngle(90);
        holder.graphView.getGridLabelRenderer().setNumHorizontalLabels(m.size());
        holder.graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));

        holder.graphView.getViewport().setXAxisBoundsManual(true);
        holder.graphView.getViewport().setMinX(m.getFirst().timeTaken / 1000L);
        holder.graphView.getViewport().setMaxX(m.getLast().timeTaken / 1000L);
        holder.graphView.getGridLabelRenderer().setHumanRounding(false);

        holder.expandButton.setOnClickListener(v-> {
            View listView = v.getRootView().findViewById(R.id.measurementListView);

            listView.setVisibility(listView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE );

            ImageButton expandButton = v.getRootView().findViewById(R.id.expandButton);

            expandButton.setRotation(listView.getVisibility() == View.VISIBLE ? 0 : 180 );
        });

    }

    @Override
    public int getItemCount(){
        return measurements.size();
    }

    // Inner ViewHolder class:
    // Holds references to the cover ImageView and title TextView.
    static class MeasurementViewHolder extends RecyclerView.ViewHolder {
        TextView title, pbDisplay;

        ImageButton expandButton;

        GraphView graphView;

        List<Measurement> measurements;

        String bodyPart;

        MeasurementViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.bodyPartName);
            expandButton = v.findViewById(R.id.expandButton);
            pbDisplay = v.findViewById(R.id.measurementList);
            graphView = v.findViewById(R.id.measurementGraph);
        }
    }

}

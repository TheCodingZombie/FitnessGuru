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

    public interface Click {
        void open(View shared);
    }
    private final Context context;
    private final Click click;
    private List<List<Measurement>> measurements = new ArrayList<>();
    private List<String> bodyParts = new ArrayList<>();

    // Constructor accepts the workout list and a workout callback.
    public MeasurementAdapter(Context c, Click l) { context = c; click = l; }
    public void setMeasurements(List<List<Measurement>> ml) {
        measurements = new ArrayList<>(ml);
        notifyDataSetChanged();
    }

    public void setBodyParts(List<String> sl) {
        bodyParts = new ArrayList<>(sl);
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

        holder.measurements = m;
        holder.bodyPart = bodyParts.get(position);

        holder.title.setText(holder.bodyPart);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        for (Measurement M : holder.measurements){
            series.appendData(new DataPoint(new Date(M.timeTaken / 1000L), M.quantity), true, 99);
        }

        if (!holder.measurements.isEmpty()) {
            holder.pbDisplay.setText(
                    "Current Measurement: " + holder.measurements.getLast().quantity + " " + holder.measurements.getLast().unit
            );

            holder.graphView.setTitle("Body Part History");

            holder.graphView.setTitleTextSize(40);

            holder.graphView.addSeries(series);

            series.setDrawBackground(true);
            series.setDrawDataPoints(true);

            holder.graphView.getGridLabelRenderer().setHorizontalLabelsAngle(90);
            holder.graphView.getGridLabelRenderer().setNumHorizontalLabels(m.size());
            holder.graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context));

            holder.graphView.getViewport().setXAxisBoundsManual(true);
            holder.graphView.getViewport().setYAxisBoundsManual(true);
            holder.graphView.getViewport().setMinX(m.getFirst().timeTaken / 1000L);
            holder.graphView.getViewport().setMaxX(m.getLast().timeTaken / 1000L);
            holder.graphView.getViewport().setMinY(0);
            holder.graphView.getViewport().setMaxY(holder.getMax());
            holder.graphView.getGridLabelRenderer().setHumanRounding(false);
        }

        holder.expandButton.setOnClickListener(v-> click.open(holder.itemView));
        holder.expandButton.setVisibility(holder.measurements.isEmpty() ? View.GONE : View.VISIBLE);

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

        double getMax() {
            double res = -999;

            for (Measurement m : measurements){
                if (m.quantity > res) res = m.quantity;
            }

            return res;
        }
    }

}

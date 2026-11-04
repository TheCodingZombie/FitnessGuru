package com.undead.fitnessguru;

import android.content.Context;
import android.net.Uri;
import android.opengl.Visibility;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.SetViewHolder> {

    public interface Click {

        void weightChanged(Exercise.Set s, String text);

        void repsChanged(Exercise.Set s, String text);
        void delete(Exercise.Set s);

        void copy(Exercise.Set s);
    }

    private final Context context;

    private final Click click;
    private List<Exercise.Set> sets = new ArrayList<>();

    // Constructor accepts the workout list and a callback.
    public ExerciseAdapter(Context c, Click l) { context = c; click = l; }
    public void setSets(List<Exercise.Set> list) {
        sets = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public SetViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType
    ){
        return new SetViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_set, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            SetViewHolder holder, int position
    ){
        Exercise.Set s = sets.get(position);

        holder.copy.setVisibility(sets.getLast() == s ? View.VISIBLE : View.GONE);

        if (s.weight != -99) holder.weight.setText(Float.toString(s.weight));
        if (s.reps != -99) holder.reps.setText(Integer.toString(s.reps));

        holder.index.setText((position + 1) + ". ");
        holder.delete.setOnClickListener(v->click.delete(s));

        holder.copy.setOnClickListener(v->click.copy(s));

        holder.weight.addTextChangedListener(
                new TextWatcher(){
                     public void beforeTextChanged(CharSequence s,int st,int c,int a){}
                     public void onTextChanged(CharSequence sq,int st,int before,int count){
                         click.weightChanged(s, holder.weight.getText().toString().trim());
                     }
                     public void afterTextChanged(Editable e){}
                 }
        );

        holder.reps.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence sq, int start, int before, int count) {
                        click.repsChanged(s, holder.reps.getText().toString().trim());
                    }
                }
        );
    }

    @Override
    public int getItemCount(){
        return sets.size();
    }

    // Inner ViewHolder class:
    // Holds references to the cover ImageView and title TextView.
    static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView index;

        ImageButton delete, copy;

        EditText weight, reps;

        SetViewHolder(View v){
            super(v);
            index = v.findViewById(R.id.numberOfSet);
            delete = v.findViewById(R.id.deleteSet);
            copy = v.findViewById(R.id.copySet);
            weight = v.findViewById(R.id.weightInput);
            reps = v.findViewById(R.id.repsInput);
        }
    }
}
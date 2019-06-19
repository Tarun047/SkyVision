package com.tarun.skyvision;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherRecycler extends RecyclerView.Adapter<WeatherRecycler.ViewHolder> {

    private List<Integer> mIcons;
    private List<String> mPredictions;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    WeatherRecycler(Context context, List<Integer> icons, List<String> predictions) {
        this.mInflater = LayoutInflater.from(context);
        this.mIcons = icons;
        this.mPredictions = predictions;
    }

    // inflates the row layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.weather_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String prediction = mPredictions.get(position);
        holder.myTextView.setText(prediction);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View myView;
        TextView myTextView;
        View rootView;
        ViewHolder(View itemView) {
            super(itemView);
            rootView=itemView;
            myTextView = itemView.findViewById(R.id.wetherDesc);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mPredictions.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
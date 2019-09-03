package com.husin.bokingkangbarers.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.husin.bokingkangbarers.Model.TimeSlot;

import java.sql.Time;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time_slot,txt_time_slot_description;
        CardView card_time_slot;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = (CardView)itemView.findViewById(com.husin.bokingkangbarers.R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(com.husin.bokingkangbarers.R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(com.husin.bokingkangbarers.R.id.txt_time_slot_description);

        }
    }
}

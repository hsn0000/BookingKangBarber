package com.husin.bokingkangbarers.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Interface.IRecyclerItemSelectedListener;
import com.husin.bokingkangbarers.Model.TimeSlot;
import com.husin.bokingkangbarers.R;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_time_slot.setText(new StringBuilder(Common.converTimeSlotToString(i)).toString());
        if (timeSlotList.size() == 0) // jika semua posisi tersedia
        {

            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));

            myViewHolder.txt_time_slot_description.setText("Tersedia");
            myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
              .getColor(android.R.color.black));
            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));


        }
        else   // jika posisi penuh (booked)
        {
            for(TimeSlot slotValue:timeSlotList)
            {
                // loop semua waktu slot dari server dan set diffrent color
               int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i) // jika slot == posisi
                {
                    // set tag untuk semua slot waktu yg pull
                    // *****

                    myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                    myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                    myViewHolder.txt_time_slot_description.setText("Full");
                    myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                            .getColor(android.R.color.white));
                    myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                }
            }
        }
        // menambahkan semua kartu ke list (20 kartu)
        // tida menambahkan kartu yg siap cardViewList
        if (!cardViewList.contains(myViewHolder.card_time_slot))
            cardViewList.add(myViewHolder.card_time_slot);

        // cek jika kartu masih tersedia
        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                // loop semua kartu dalam daftar kartu
                for (CardView cardView:cardViewList)
                {
                    if (cardView.getTag() == null) // kartu langsung tersedia untuk memilih selot
                        cardView.setBackgroundColor(context.getResources()
                                .getColor(android.R.color.white));
                }
                // kartu berubah warna saat di pilih
                myViewHolder.card_time_slot.setBackgroundColor(context.getResources()
                .getColor(android.R.color.holo_orange_dark));

                // sebelumnya kirim broadcast ke enable button NEXT
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_TIME_SLOT,i); // menempatkan indeks slot waktu yang telah di pilih
                intent.putExtra(Common.KEY_STEP, 3); // pergi ke langkah 3
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot,txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener ;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = (CardView)itemView.findViewById(com.husin.bokingkangbarers.R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(com.husin.bokingkangbarers.R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(com.husin.bokingkangbarers.R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }
}

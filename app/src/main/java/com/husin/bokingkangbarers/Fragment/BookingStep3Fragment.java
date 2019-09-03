package com.husin.bokingkangbarers.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Interface.ITimeSlotLoadListener;
import com.husin.bokingkangbarers.Model.TimeSlot;
import com.husin.bokingkangbarers.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    //variable
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog dialog;

    Unbinder unbinder;
    LocalBroadcastManager localBroadcastManager;
    Calendar selected_date;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calenderView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;

    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0); // add current date
            loadAvailableTimeSlotofBarber(Common.currentBarber.getBarberId(),
                    simpleDateFormat.format(date.getTime()));

        }
    };

    private void loadAvailableTimeSlotofBarber(String barberId, String date) {
    }


    static BookingStep3Fragment instance;

    public static BookingStep3Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep3Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iTimeSlotLoadListener = this;
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_booking_step_three,container, false);
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {

    }

    @Override
    public void onTimeSlotLoadFailed(String message) {

    }

    @Override
    public void onTimeSlotLoadEmpty() {

    }
}

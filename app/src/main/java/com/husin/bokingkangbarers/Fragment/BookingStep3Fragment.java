package com.husin.bokingkangbarers.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.bokingkangbarers.Adapter.MyTimeSlotAdapter;
import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Common.SpacesItemDecoration;
import com.husin.bokingkangbarers.Interface.ITimeSlotLoadListener;
import com.husin.bokingkangbarers.Model.TimeSlot;
import com.husin.bokingkangbarers.R;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    //variable
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog dialog;

    Unbinder unbinder;
    LocalBroadcastManager localBroadcastManager;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
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

    private void loadAvailableTimeSlotofBarber(String barberId, final String bookDate) {
        dialog.show();

        // /AllSalon/Bogor/Branch/AKDLBvEHoyjRmrPAQONq/Barber/5fs1q0W5oqGqFjkBd1ht  'Agus'
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId());

        // ambil information di barber ini
        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               if(task.isSuccessful())
               {
                   DocumentSnapshot documentSnapshot = task.getResult();
                   if(documentSnapshot.exists()) // jika barber tersedia
                   {
                       // ambil informasi dari pemesanan
                       //  jika tida di buat, kembalikan kosong
                       CollectionReference date =  FirebaseFirestore.getInstance()
                               .collection("AllSalon")
                               .document(Common.city)
                               .collection("Branch")
                               .document(Common.currentSalon.getSalonId())
                               .collection("Barber")
                               .document(Common.currentBarber.getBarberId())
                               .collection(bookDate); // format tanggal sederhana  dengan dd_MM_yyyy 04_09_2019

                       date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                               if (task.isSuccessful())
                               {
                                   QuerySnapshot querySnapshot = task.getResult();
                                   if (querySnapshot.isEmpty()) // jika tida punya janji apapun
                                       iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                   else
                                   {
                                        // jika punya janji
                                       List<TimeSlot> timeSlots = new ArrayList<>();
                                       for (QueryDocumentSnapshot document:task.getResult())
                                           timeSlots.add(document.toObject(TimeSlot.class));
                                       iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                   }
                               }

                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                           }
                       });

                   }
               }

            }
        });

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
        localBroadcastManager.registerReceiver(displayTimeSlot,new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));

        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // 28_03_2019 (this is key)

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();


    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_three,container, false);
          unbinder = ButterKnife.bind(this,itemView);

          init(itemView);

          
          return itemView;
    }

    private void init(View itemView) {
        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        // calendar
        Calendar starDate = Calendar.getInstance();
        starDate.add(Calendar.DATE,0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE,2); // 2 hari keluar

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(itemView,R.id.calendarView)
                .range(starDate,endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(starDate)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis())
                {
                    Common.bookingDate = date; // kode ini tida akan meload lagi jika memilih hari baru dengan hari yg di pilih
                    loadAvailableTimeSlotofBarber(Common.currentBarber.getBarberId(),
                            simpleDateFormat.format(date.getTime()));
                }
            }
        });


    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(),timeSlotList);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();


    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
        dialog.dismiss();

    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();

    }
}

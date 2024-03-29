package com.husin.bokingkangbarers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.bokingkangbarers.Adapter.MyViewPagerAdapter;
import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Common.NonSwipeViewPager;
import com.husin.bokingkangbarers.Model.Barber;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class BookingActivity extends AppCompatActivity {

    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    CollectionReference barberRef;

    @BindView(R.id.step_view)
    StepView stepView;
    @BindView(R.id.view_pager)
    NonSwipeViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;

    //event
    @OnClick(R.id.btn_previous_step)
    void previousStep() {
        if (Common.step == 3 || Common.step > 0)
        {
            Common.step--;
            viewPager.setCurrentItem(Common.step);
            if (Common.step < 3) // alway enable next when step <3
            {
                btn_next_step.setEnabled(true);
                setColorButton();
            }
        }
    }
    @OnClick(R.id.btn_next_step)
    void nextClick() {
      if (Common.step < 3 || Common.step == 0 )
      {
         Common.step++; // increase
          if (Common.step == 1) // After chose salon
          {
              if (Common.currentSalon != null)
                  loadBarberBySalon(Common.currentSalon.getSalonId());
          }
          else if (Common.step == 2) // memilih waktu
          {
              if (Common.currentBarber != null)
                  loadTimeSlotOfBarber(Common.currentBarber.getBarberId());
          }
          else if (Common.step == 3) // confirmasi
          {
              if (Common.currentTimeSlot != -1)
                  confirmBooking();
          }
          viewPager.setCurrentItem(Common.step);
      }
    }

    private void confirmBooking() {
        //kirim broadcash to fragmentStep 4
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);

    }

    private void loadTimeSlotOfBarber(String barberId) {
        // kirim siaran local ke pragmentStep 3
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void loadBarberBySalon(String salonId) {
        dialog.show();

        // pilih semua salon barber
        // AllSalon/Bogor/Branch/AKDLBvEHoyjRmrPAQONq/Barber
        if (!TextUtils.isEmpty(Common.city))
        {
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.city)
                    .collection("Branch")
                    .document(salonId)
                    .collection("Barber");

            barberRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<Barber>barbers = new ArrayList<>();
                            for (QueryDocumentSnapshot barberSnapShot:task.getResult())
                            {
                                Barber barber = barberSnapShot.toObject(Barber.class);
                                barber.setPassword(""); // remove password in client app
                                barber.setBarberId(barberSnapShot.getId()); // get id of barber

                                barbers.add(barber);
                            }
                            //send Boadcast to bookingstep2fragment to load recycler
                             Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                            intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE,barbers);
                            localBroadcastManager.sendBroadcast(intent);

                            dialog.dismiss();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                           dialog.dismiss();
                        }
                    });

        }
    }


    //Broadcast Receiver
    private BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

          int step = intent.getIntExtra(Common.KEY_STEP,0);
          if (step == 1) //
              Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
          else if (step == 2)
              Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);
          else if (step == 3)
              Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT, -1);

          btn_next_step.setEnabled(true);
          setColorButton();
      }
  };

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        ButterKnife.bind(BookingActivity.this);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(buttonNextReceiver,new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));

        setupStepView();
        setColorButton();

        // view
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4); // we have 4 pragment so we need keep state of this screen page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {


            }

            @Override
            public void onPageSelected(int i) {

                // show step
                stepView.go(i,true);
                if ( i == 0)
                    btn_previous_step.setEnabled(false);
                else
                    btn_previous_step.setEnabled(true);

                // set disable button disini
                btn_next_step.setEnabled(false);
                setColorButton();

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void setColorButton() {
        if (btn_next_step.isEnabled())
        {
            btn_next_step.setBackgroundResource(R.color.colorButton);
        }
        else
        {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if (btn_previous_step.isEnabled())
        {
            btn_previous_step.setBackgroundResource(R.color.colorButton);
        }
        else
        {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }


    }

    private void setupStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Salon");
        stepList.add("Tukang Cukur");
        stepList.add("Waktu");
        stepList.add("Konfirmasi");
        stepView.setSteps(stepList);
    }
}

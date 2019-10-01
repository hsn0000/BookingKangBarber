package com.husin.bokingkangbarers.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Model.BookingInformation;
import com.husin.bokingkangbarers.Model.FCMResponse;
import com.husin.bokingkangbarers.Model.FCMSendData;
import com.husin.bokingkangbarers.Model.MyNotification;
import com.husin.bokingkangbarers.Model.MyToken;
import com.husin.bokingkangbarers.R;
import com.husin.bokingkangbarers.Retrofit.IFCMApi;
import com.husin.bokingkangbarers.Retrofit.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookingStep4Fragment extends Fragment {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    Unbinder unbinder;

    IFCMApi ifcmApi;

    AlertDialog dialog;

    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;

    @OnClick(R.id.btn_confirm)
            void confoirmBooking() {
        dialog.show();

        // proses timestamp
        // timstamp di gunakan untuk mempilter semua booking dengan tanggal dan hari
        // di tampilkan di semua tampilan dan pitur booking
        String startTime = Common.converTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); // split ex : 09:00 - 10:00
        // Get start time : get 09:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // ambil 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // ambil 00

        Calendar bookingDateWithourHouse = Calendar.getInstance();
        bookingDateWithourHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithourHouse.set(Calendar.HOUR_OF_DAY,startHourInt); //
        bookingDateWithourHouse.set(Calendar.MINUTE,startMinInt);

        // buat timestamp object dan gunakan ke bookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithourHouse.getTime());

        // membuat booking information
        final BookingInformation bookingInformation = new BookingInformation();

        bookingInformation.setCityBook(Common.city);
        bookingInformation.setTimestamp(timestamp);
        bookingInformation.setDone(false); // semua false, fild ini di gunakan di semua untuk menyaring tampilan
        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.converTimeSlotToString(Common.currentTimeSlot))
                .append(" WIB ")
                .append(simpleDateFormat.format(bookingDateWithourHouse.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));

        //submit ke barber document
        DocumentReference bookingDate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barber")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
        .document(String.valueOf(Common.currentTimeSlot));

        // buat data
        bookingDate.set(bookingInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // membuat pungsi cek jika pesanan sudah ada
                        // maka kembalikan ke event booking untuk memilih jam kosong
                      addToUserBooking(bookingInformation);
                      
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToUserBooking(final BookingInformation bookingInformation) {

        // buat coleksi baru
        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        // cek jika document sudah ada di coleksi ini
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());
        userBooking
                .whereGreaterThanOrEqualTo("timestamp",toDayTimeStamp)
                .whereEqualTo("done",false)
                .limit(1) // only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty())
                        {
                            // set data
                            userBooking.document()
                                    .set(bookingInformation)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {


                                            // buat notifikasi
                                            MyNotification myNotification = new MyNotification();
                                            myNotification.setUid(UUID.randomUUID().toString());
                                            myNotification.setTitle("Pesanan Terbaru");
                                            myNotification.setContent("Anda memiliki janji baru untuk perawatan & potong rambut,dengan Bpk/Sdr : "+Common.currentUser.getName());
                                            myNotification.setRead(false); // menyaring notifikasi dengan "read" is false on barber staf
                                            myNotification.setServerTimestamp(FieldValue.serverTimestamp());

                                            // notifikasi submit untuk 'notifikation' collection barber
                                            FirebaseFirestore.getInstance()
                                                    .collection("AllSalon")
                                                    .document(Common.city)
                                                    .collection("Branch")
                                                    .document(Common.currentSalon.getSalonId())
                                                    .collection("Barber")
                                                    .document(Common.currentBarber.getBarberId())
                                                    .collection("Notifications") //**
                                                    .document(myNotification.getUid()) // buat unique key
                                                    .set(myNotification)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                           //get Token base on barber id
                                                            FirebaseFirestore.getInstance()
                                                                    .collection("Tokens")
                                                                    .whereEqualTo("userPhone",Common.currentBarber.getUsername())
                                                                    .limit(1)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                            if (task.isSuccessful() && task.getResult().size() > 0 )
                                                                            {
                                                                                MyToken myToken = new MyToken();
                                                                                for (DocumentSnapshot tokenSnapShot : task.getResult())
                                                                                    myToken = tokenSnapShot.toObject(MyToken.class);

                                                                                //membuat data untuk mengirim
                                                                                FCMSendData sendRequest = new FCMSendData();
                                                                                Map<String,String> dataSend = new HashMap<>();
                                                                                dataSend.put(Common.TITLE_KEY, "Pesanan Baru");
                                                                                dataSend.put(Common.CONTENT_KEY, "Anda memiliki pesanan baru dari pelanggan"+Common.currentUser.getName());

                                                                                sendRequest.setTo(myToken.getToken());
                                                                                sendRequest.setData(dataSend);

                                                                             compositeDisposable.add(ifcmApi.sendNotification(sendRequest)
                                                                                     .subscribeOn(Schedulers.io())
                                                                                     .observeOn(AndroidSchedulers.mainThread())
                                                                                     .subscribe(new Consumer<FCMResponse>() {
                                                                                         @Override
                                                                                         public void accept(FCMResponse fcmResponse) throws Exception {

                                                                                             dialog.dismiss();

                                                                                             addToCalendar(Common.bookingDate,
                                                                                                     Common.converTimeSlotToString(Common.currentTimeSlot));
                                                                                             resetStaticData();
                                                                                             getActivity().finish();  // close activitiy
                                                                                             Toast.makeText(getContext(),"Trimakasih, Pesanan Anda Segera Di Proses!!",Toast.LENGTH_SHORT).show();


                                                                                         }
                                                                                     }, new Consumer<Throwable>() {
                                                                                         @Override
                                                                                         public void accept(Throwable throwable) throws Exception {
                                                                                             Log.d("NOTIFICATION_ERROR", throwable.getMessage());
                                                                                             addToCalendar(Common.bookingDate,
                                                                                                     Common.converTimeSlotToString(Common.currentTimeSlot));
                                                                                             resetStaticData();
                                                                                             getActivity().finish();  // close activitiy
                                                                                             Toast.makeText(getContext(),"Trimakasih, Pesanan Anda Segera Di Proses!!",Toast.LENGTH_SHORT).show();

                                                                                         }
                                                                                     }));

                                                                            }

                                                                        }
                                                                    });

                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if (dialog.isShowing())
                                                dialog.dismiss();
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else
                        {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            resetStaticData();
                            getActivity().finish();  // close activitiy
                            Toast.makeText(getContext(),"Trimakasih, Pesanan Anda Segera Di Proses!!",Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void addToCalendar(Calendar bookingDate, String starDate) {
        String startTime = Common.converTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-"); // split ex : 09:00 - 10:00
        // Get start time : get 09:00
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // ambil 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // ambil 00

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim()); // ambil 10
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim()); // ambil 00

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY,startHourInt); // set event start Hour
        startEvent.set(Calendar.MINUTE,startMinInt); // set event start min

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY,endHourInt); // set event start Hour
        endEvent.set(Calendar.MINUTE,endMinInt); // set event start min

        // sebelum startEvent dan endEvent, convert terlebih dahulu ke format string
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calendarDateFormat.format(startEvent.getTime());
        String endEventTime = calendarDateFormat.format(endEvent.getTime());

        addToDeviceCalendar(startEventTime,endEventTime," Pemesanan Potong Rambut ",
                new StringBuilder(" Potong rambut dari jam ")
        .append(startTime)
        .append(" Dengan Bapak ")
        .append(Common.currentBarber.getName())
        .append(" Di ")
        .append(Common.currentSalon.getName()).toString(),
                new StringBuilder(" Alamat Barber : ").append(Common.currentSalon.getAddress()).toString());
    }

    private void addToDeviceCalendar(String startEventTime, String endEventTime, String title, String description, String location) {
        SimpleDateFormat calendarDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        try {
            Date start = calendarDateFormat.parse(startEventTime);
            Date end = calendarDateFormat.parse(endEventTime);

            ContentValues event = new ContentValues();

            // put
            event.put(CalendarContract.Events.CALENDAR_ID,getCalendar(getContext()));
            event.put(CalendarContract.Events.TITLE,title);
            event.put(CalendarContract.Events.DESCRIPTION,description);
            event.put(CalendarContract.Events.EVENT_LOCATION,location);

            // Time
            event.put(CalendarContract.Events.DTSTART,start.getTime());
            event.put(CalendarContract.Events.DTEND,end.getTime());
            event.put(CalendarContract.Events.ALL_DAY,0);
            event.put(CalendarContract.Events.HAS_ALARM,1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE,timeZone);

            Uri calendars;
            if(Build.VERSION.SDK_INT >= 8)
               calendars = Uri.parse("content://com.android.calendar/events");
            else
                calendars = Uri.parse("content://calendar/events");

             Uri uri_save = getActivity().getContentResolver().insert(calendars,event);
             // simpan ke cache
            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE,uri_save.toString());


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getCalendar(Context context) {
        // get default calendar ID dari calendar GMAIL
        String gmailIdCalendar = "";
        String projection[]={"_id","calendar_displayName"};
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        ContentResolver contentResolver = context.getContentResolver();
        // pilih semua calendar
        Cursor managedCursor = contentResolver.query(calendars,projection,null,null,null);
        if (managedCursor.moveToFirst())
        {
            String callName;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                callName = managedCursor.getString(nameCol);
                if (callName.contains("@gmail.com"))
                {
                    gmailIdCalendar = managedCursor.getString(idCol);
                    break; // Exit as soon as have id
                }
            }while (managedCursor.moveToNext());
            managedCursor.close();
        }

        return gmailIdCalendar;
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE,0); // menambahkan curren datai
    }


    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           setData();
        }
    };

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.converTimeSlotToString(Common.currentTimeSlot))
        .append(" WIB ")
        .append(simpleDateFormat.format(Common.bookingDate.getTime())));

        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
    }


    static BookingStep4Fragment instance;

    public static BookingStep4Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep4Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifcmApi = RetrofitClient.getInstance().create(IFCMApi.class);

        // gunakan format tanggal untuk di tampilkan saat Confirmasi
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false)
                .build();
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

      View itemView = inflater.inflate(R.layout.fragment_booking_step_four,container, false);
      unbinder = ButterKnife.bind(this, itemView);
      return itemView;

    }
}

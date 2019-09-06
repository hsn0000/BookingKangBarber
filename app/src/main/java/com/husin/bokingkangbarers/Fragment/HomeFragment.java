package com.husin.bokingkangbarers.Fragment;


import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.bokingkangbarers.Adapter.HomeSliderAdapter;
import com.husin.bokingkangbarers.Adapter.LookbookAdapter;
import com.husin.bokingkangbarers.BookingActivity;
import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Database.CartDatabase;
import com.husin.bokingkangbarers.Database.DatabaseUtils;
import com.husin.bokingkangbarers.Interface.IBannerLoadListener;
import com.husin.bokingkangbarers.Interface.IBookingInfoLoadListener;
import com.husin.bokingkangbarers.Interface.IBookingInformationChangeListener;
import com.husin.bokingkangbarers.Interface.ICountItemInCartListener;
import com.husin.bokingkangbarers.Interface.ILoookbookLoadListener;
import com.husin.bokingkangbarers.Model.Banner;
import com.husin.bokingkangbarers.Model.BookingInformation;
import com.husin.bokingkangbarers.R;
import com.husin.bokingkangbarers.Service.PicassoImageLoadingService;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ss.com.bannerslider.Slider;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements ILoookbookLoadListener, IBannerLoadListener, IBookingInfoLoadListener, IBookingInformationChangeListener, ICountItemInCartListener {

    private Unbinder unbinder;

    CartDatabase cartDatabase;

    AlertDialog dialog;

    @BindView(R.id.notification_badge)
    NotificationBadge notificationBadge;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;

    @OnClick(R.id.btn_delete_booking)
    void deleteBooking()
    {
        deleteBookingFromBarber(false);
    }

    @OnClick(R.id.btn_change_booking)
    void chageBooking() {
        changeBookingFromUser();
    }

    private void changeBookingFromUser() {
        // show dialog confirm
        androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("Hallo!")
                .setMessage("Anda benar-benar ingin mengubah informasi pemesanan?\n" +
                        "Karena kami akan menghapus informasi pemesanan lama Anda..\n" +
                        "KONFIRMASI SAJAH!")
                .setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton("LANJUT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteBookingFromBarber(true); // true karna memanggil saat ada pilihan
                    }
                });
        confirmDialog.show();
    }

    private void deleteBookingFromBarber(boolean isChange) {
        // hapus booking dari barber collection, delete event
        // load Common.currentBooking dari data bookingInformation
        if (Common.currentBooking != null)
        {
            dialog.show();
           // get booking information in barber objek
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.currentBooking.getCityBook())
                    .collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barber")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());
            // when have document , just delete it
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // sebelum mengahapus Barber done
                    // akan mulai menghapus dari user
                    deleteBookingFromUser(isChange);
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Current Booking must not be null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBookingFromUser(boolean isChange) {
        // pertama ambil informasi dari object user
        if (!TextUtils.isEmpty(Common.currentBookingId))
        {
          DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                  .collection("User")
                  .document(Common.currentUser.getPhoneNumber())
                  .collection("Booking")
                  .document(Common.currentBookingId);

          // hapus/batalkan
            userBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // sebelum menghapus dari user perlu mengahapus dari kalendar
                    // ambil save Uri dari Event add
                    Paper.init(getActivity());
                    Uri eventUri = Uri.parse(Paper.book().read(Common.EVENT_URI_CACHE).toString());
                    getActivity().getContentResolver().delete(eventUri,null,null);

                    Toast.makeText(getActivity(), "Pembatalan pesanan telah berhasil !", Toast.LENGTH_SHORT).show();

                    //refresh
                    loadUserBooking();

                    // cek jika isChange -> memanggil dari tombol perubahan. fired interface
                    if (isChange)
                        iBookingInformationChangeListener.onBookingInformationChange();

                    dialog.dismiss();
                }
            });

        }
        else
        {
            dialog.dismiss();

            Toast.makeText(getContext(), "Booking information ID must not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.card_view_booking)
    void booking()

    {
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    // firestore
    CollectionReference bannerRef,lookbookRef;

    //Interface
    IBannerLoadListener iBannerLoadListener;
    ILoookbookLoadListener iLoookbookLoadListener;
    IBookingInfoLoadListener iBookingInfoLoadListener;
    IBookingInformationChangeListener iBookingInformationChangeListener;


    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");

    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        countCartItem();
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentUser.getPhoneNumber())
                .collection("Booking");

        // get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        // pilih informasi booking dari firebase dengan done=false dan Timstamp grater today
        userBooking
                .whereGreaterThanOrEqualTo("timestamp",toDayTimeStamp)
                .whereEqualTo("done",false)
                .limit(1) // only take 1
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            if (!task.getResult().isEmpty())
                            {
                               for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult())
                               {
                                   BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                   iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation,queryDocumentSnapshot.getId());
                                   break; // keluar dari looping
                               }
                            }
                            else
                                iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage());

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_home, container, false);
         unbinder = ButterKnife.bind(this,view);

         cartDatabase = CartDatabase.getInstance(getContext());

          // init
        Slider.init(new PicassoImageLoadingService());
        iBannerLoadListener = this;
        iLoookbookLoadListener = this;
        iBookingInfoLoadListener = this;
        iBookingInformationChangeListener = this;

        // cek yang masuk
        if (AccountKit.getCurrentAccessToken() !=null)
        {
            setUserInformation();
            loadBanner();
            loadLookBook();
            loadUserBooking();
            countCartItem();
        }


         return view;
    }

    private void countCartItem() {
        DatabaseUtils.countItemInCart(cartDatabase,this);
    }

    private void loadLookBook() {

        lookbookRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> lookbooks = new ArrayList<>();
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot bannerSnapShot:task.getResult())
                            {
                                Banner banner = bannerSnapShot.toObject(Banner.class);
                                lookbooks.add(banner);
                            }
                            iLoookbookLoadListener.onLookbookLoadSuccess(lookbooks);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iLoookbookLoadListener.onLookbookLoadFailed(e.getMessage());
            }
        });

    }

    private void loadBanner()  {
        bannerRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Banner> banners = new ArrayList<>();
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot bannerSnapShot:task.getResult())
                            {
                                Banner banner = bannerSnapShot.toObject(Banner.class);
                                banners.add(banner);
                            }
                            iBannerLoadListener.onBannerLoadSuccess(banners);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                  iBannerLoadListener.onBannerLoadFailed(e.getMessage());
            }
        });
    }

    private void setUserInformation() {
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
    }

    @Override
    public void onLookbookLoadSuccess(List<Banner> banners) {

        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookbookAdapter(getActivity(),banners));

    }

    @Override
    public void onLookbookLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));

    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.GONE);

    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation,String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;

        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(),0).toString();

        txt_time_remain.setText(dateRemain);

        card_booking_info.setVisibility(View.VISIBLE); //***

            dialog.dismiss();
    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(getContext(),message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBookingInformationChange() {
        // memulai startActivityBooking
        startActivity(new Intent(getActivity(),BookingActivity.class));

    }

    @Override
    public void onCartItemCountSuccess(int count) {
        notificationBadge.setText(String.valueOf(count));
    }
}

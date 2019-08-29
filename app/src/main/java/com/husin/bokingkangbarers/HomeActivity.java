package com.husin.bokingkangbarers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.husin.bokingkangbarers.Common.Common;
import com.husin.bokingkangbarers.Fragment.HomeFragment;
import com.husin.bokingkangbarers.Fragment.ShopingFragment;
import com.husin.bokingkangbarers.Model.User;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        // init

        userRef = FirebaseFirestore.getInstance().collection("User");
        dialog = new SpotsDialog.Builder().setContext(this).build();

        // chek intent
        if (getIntent() != null)
        {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if(isLogin)
            {
                dialog.show();
                // cek jika user telah ada
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        if (account != null)
                        {
                            DocumentReference currentUser = userRef.document(account.getPhoneNumber().toString());
                            currentUser.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                           if (task.isSuccessful())
                                           {
                                               DocumentSnapshot userSnapShot = task.getResult();
                                               if (!userSnapShot.exists())
                                               {
                                                   showUpdateDialog(account.getPhoneNumber().toString());
                                               }
                                               else
                                               {
                                                   // jika user sudah siap
                                                   Common.currentUser = userSnapShot.toObject(User.class);
                                                   bottomNavigationView.setSelectedItemId(R.id.action_home);
                                               }
                                               if (dialog.isShowing())
                                                   dialog.dismiss();

                                           }
                                        }
                                    });
                        }

                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(HomeActivity.this, ""+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT);

                    }
                });
            }
        }
        //     view
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
               if (menuItem.getItemId() == R.id.action_home)
                   fragment = new HomeFragment();
               else if (menuItem.getItemId() == R.id.action_shopping)
                   fragment = new ShopingFragment();

                return loadFragment(fragment);
            }
        });


    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(String phoneNumber) {

        // init dialog

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One More Step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information,null);

        Button btn_update = (Button)sheetView.findViewById(R.id.btn_update);
        TextInputEditText edt_name = (TextInputEditText)sheetView.findViewById(R.id.edt_name);
        TextInputEditText edt_address = (TextInputEditText)sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!dialog.isShowing())
                    dialog.show();

                User user = new User(edt_name.getText().toString(),
                        edt_address.getText().toString(),phoneNumber);
                userRef.document(phoneNumber)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                bottomSheetDialog.dismiss();
                                if (dialog.isShowing())
                                    dialog.dismiss();

                                Common.currentUser = user;
                                bottomNavigationView.setSelectedItemId(R.id.action_home);

                                Toast.makeText(HomeActivity.this, "Thank You", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                      }
                   });

            }
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

    }
}

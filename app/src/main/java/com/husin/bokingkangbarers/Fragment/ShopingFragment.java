package com.husin.bokingkangbarers.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.husin.bokingkangbarers.Adapter.MyShoppingItemAdapter;
import com.husin.bokingkangbarers.Common.SpacesItemDecoration;
import com.husin.bokingkangbarers.Interface.IShoppingDataLoadListener;
import com.husin.bokingkangbarers.Model.ShoppingItem;
import com.husin.bokingkangbarers.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShopingFragment extends Fragment implements IShoppingDataLoadListener {

    CollectionReference shoppingItemRef;

    IShoppingDataLoadListener iShoppingDataLoadListener;

    Unbinder unbinder;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;

    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @OnClick(R.id.chip_wax)
    void waxChipClick() {
        setSelectedChip(chip_wax);
        loadShoppingItem("Wax");
    }

    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @OnClick(R.id.chip_spray)
    void sprayChipClick() {
        setSelectedChip(chip_spray);
        loadShoppingItem("Spray");
    }

    @BindView(R.id.chip_body_care)
    Chip chip_body_care;
    @OnClick(R.id.chip_body_care)
    void bodyCareChipClick() {
        setSelectedChip(chip_body_care);
        loadShoppingItem("BodyCare");
    }

    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;
    @OnClick(R.id.chip_hair_care)
    void hairCareChipClick() {
        setSelectedChip(chip_hair_care);
        loadShoppingItem("HairCare");
    }

    @BindView(R.id.recycler_items)
    RecyclerView recycler_items;

    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance().collection("Shopping")
                .document(itemMenu)
                .collection("Items");
        // get data
        shoppingItemRef.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage());
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
               if (task.isSuccessful())
               {
                   List<ShoppingItem> shoppingItems = new ArrayList<>();
                   for (DocumentSnapshot itemSnapShot:task.getResult())
                   {
                       ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                       shoppingItem.setId(itemSnapShot.getId()); //  get null refrence
                       shoppingItems.add(shoppingItem);
                   }
                   iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
               }
            }
        });
    }

    private void setSelectedChip(Chip chip) {
        // set color
        for (int i=0;i<chipGroup.getChildCount();i++)
        {
            Chip chipItem = (Chip)chipGroup.getChildAt(i);
            if (chipItem.getId() != chip.getId()) // jika tida di pilih
            {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            else // jika di pilih
            {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    public ShopingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    View itemView = inflater.inflate(R.layout.fragment_shoping, container, false);

    unbinder = ButterKnife.bind(this,itemView);
    iShoppingDataLoadListener = this;

    // load default
        loadShoppingItem("Wax");

    initView();

    return itemView;

    }

    private void initView() {
        recycler_items.setHasFixedSize(true);
        recycler_items.setLayoutManager(new GridLayoutManager(getContext(),2));
        recycler_items.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(),shoppingItemList);
        recycler_items.setAdapter(adapter);

    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(),    message, Toast.LENGTH_SHORT).show();
    }
}

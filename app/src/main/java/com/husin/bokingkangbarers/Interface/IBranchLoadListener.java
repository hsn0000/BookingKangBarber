package com.husin.bokingkangbarers.Interface;

import com.husin.bokingkangbarers.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);

}

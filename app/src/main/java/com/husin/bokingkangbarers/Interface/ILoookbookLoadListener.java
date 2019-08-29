package com.husin.bokingkangbarers.Interface;

import com.husin.bokingkangbarers.Model.Banner;

import java.util.List;

public interface ILoookbookLoadListener {
    void onLookbookLoadSuccess(List<Banner> banners);
    void onLookbookLoadFailed(String Message);
}

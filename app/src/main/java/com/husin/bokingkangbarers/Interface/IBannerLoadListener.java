package com.husin.bokingkangbarers.Interface;

import com.husin.bokingkangbarers.Model.Banner;

import java.util.List;

public interface IBannerLoadListener {
    void onBannerLoadSuccess(List<Banner>banners);
    void onBannerLoadFailed(String Message);

}

package com.husin.bokingkangbarers.Interface;

import java.util.List;

public interface IAllSalonLoadListener {
    void onAllSalonLoadSuccess(List<String>areaNameList);
    void onAllSallonLoadFailed(String message);





}

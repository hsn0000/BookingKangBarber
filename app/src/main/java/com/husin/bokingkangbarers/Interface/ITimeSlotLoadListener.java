package com.husin.bokingkangbarers.Interface;

import com.husin.bokingkangbarers.Model.TimeSlot;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess (List<TimeSlot> timeSlotList);
    void onTimeSlotLoadFailed (String message);
    void onTimeSlotLoadEmpty();
}

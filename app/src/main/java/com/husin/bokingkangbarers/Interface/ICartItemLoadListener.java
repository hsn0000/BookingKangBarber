package com.husin.bokingkangbarers.Interface;

import com.husin.bokingkangbarers.Database.CartItem;

import java.util.List;

public interface ICartItemLoadListener {
    void onGetAllItemFromCartSuccess(List<CartItem> cartItemlist);

}

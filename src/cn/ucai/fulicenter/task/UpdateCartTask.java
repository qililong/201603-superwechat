package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;

/**
 * Created by Administrator on 2016/6/22.
 */
public class UpdateCartTask extends BaseActivity{
    Context mContext;
    ArrayList<CartBean> cartList;
    String path;
    int actionType;
    CartBean mCart;

    public UpdateCartTask(Context mContext, CartBean cart) {
        this.mContext = mContext;
        this.mCart = cart;
        init();
    }

    private void init() {
        ArrayList<CartBean> cartList = FuliCenterApplication.getInstance().getCartList();
        try {
            if (cartList.contains(mCart)) {
                if (mCart.getCount() <= 0) {
                    path = new ApiParams()
                            .with(I.Cart.ID, mCart.getId() + "")
                            .getRequestUrl(I.REQUEST_DELETE_CART);
                    actionType = 0;
                } else {
                    path = new ApiParams()
                            .with(I.Cart.IS_CHECKED, mCart.isChecked() + "")
                            .with(I.Cart.COUNT, mCart.getCount() + "")
                            .with(I.Cart.ID, mCart.getId() + "")
                            .getRequestUrl(I.REQUEST_UPDATE_CART);
                    actionType = 1;
                }
            } else {
                path = new ApiParams()
                        .with(I.Cart.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                        .with(I.Cart.GOODS_ID, mCart.getGoods().getGoodsId() + "")
                        .with(I.Cart.COUNT, mCart.getCount() + "")
                        .with(I.Cart.IS_CHECKED, mCart.isChecked() + "")
                        .getRequestUrl(I.REQUEST_ADD_CART);
                actionType = 2;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                responseUpdateCartListener(), errorListener()));
    }

    private Response.Listener<MessageBean> responseUpdateCartListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    ArrayList<CartBean> carts = FuliCenterApplication.getInstance().getCartList();
                    if (actionType == 0) {
                        carts.remove(mCart);
                    }
                    if (actionType == 1) {
                        carts.set(carts.indexOf(mCart), mCart);
                    }
                    if (actionType == 2) {
                        mCart.setId(Integer.parseInt(messageBean.getMsg()));
                        carts.add(mCart);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_cart"));
                }
            }
        };
    }
}

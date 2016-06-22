package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/20.
 */
public class DownloadCartListCountTask extends BaseActivity{
    Context mContext;
    String userName;

    int pageID;
    int pageSize = 10;
    String path;
    int listSize = 0;

    ArrayList<CartBean> list;
    ArrayList<CartBean> cartList;
    public DownloadCartListCountTask(Context mContext) {
        this.mContext = mContext;
        this.userName = FuliCenterApplication.getInstance().getUserName();

        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Cart.USER_NAME,userName)
                    .with(I.PAGE_ID,pageID+"")
                    .with(I.PAGE_SIZE,pageSize+"")
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<CartBean[]>(path, CartBean[].class,
                responseDownloadCartListTaskLiskListener(), errorListener()));
    }

    private Response.Listener<CartBean[]> responseDownloadCartListTaskLiskListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBean) {
                if (cartBean != null) {
                    list = Utils.array2List(cartBean);
                    for (CartBean cart : list) {
                        try {
                            path = new ApiParams()
                                    .with(I.CategoryGood.GOODS_ID,cart.getGoodsId() + "")
                                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
                            executeRequest(new GsonRequest<GoodDetailsBean>(path, GoodDetailsBean.class,
                                    responseDownloadGoodDetailListener(cart), errorListener()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetailListener(final  CartBean cart) {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                listSize++;
                if (goodDetailsBean != null) {
                    cart.setGoods(goodDetailsBean);
                    cartList = FuliCenterApplication.getInstance().getCartList();
                    if (!cartList.contains(cart)) {
                        cartList.add(cart);
                    }
                }
                if (listSize == list.size()) {
                    FuliCenterApplication.getInstance().setCartList(list);
                    ArrayList<CartBean> cartList = FuliCenterApplication.getInstance().getCartList();
                    mContext.sendStickyBroadcast(new Intent("update_cart_list"));
                    mContext.sendStickyBroadcast(new Intent("update_cart"));
                }
            }
        };
    }
}

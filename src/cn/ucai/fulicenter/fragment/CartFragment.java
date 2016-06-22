package cn.ucai.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FulicenterActivity;
import cn.ucai.fulicenter.adapter.BoutiqueAdapter;
import cn.ucai.fulicenter.adapter.CartAdapter;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCartListCountTask;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/18.
 */
public class CartFragment extends Fragment{

    FulicenterActivity mContext;
    ArrayList<CartBean> mGoodList;
    CartAdapter mAdapter;
    private int action = I.ACTION_DOWNLOAD;
    String path;
    TextView mtvRankPrice, mtvSavePrice;
    TextView nothing;

    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    LinearLayoutManager mGridLayoutManager;

    int listSize;
    ArrayList<CartBean> list;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (FulicenterActivity) getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_cart,null);
        mGoodList = new ArrayList<CartBean>();
        initData();
        initView(layout);
//        getPath();
//        Log.e("main", "onCreateView" + path.toString());
        setListener();
        return layout;
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
    }

    /**
     * 上拉刷新事件监听
     */
    private void setPullUpRefreshListener() {
        mRecyclerView.setOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    int lastItemPosition;

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                                lastItemPosition == mAdapter.getItemCount()) {
                            if (mAdapter.isMore()) {
                                mSwipeRefreshLayout.setRefreshing(true);
                                action = I.ACTION_PULL_UP;
                                getPath();
                                mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                                        CartBean[].class, responseDownloadNewGoodListener(),
                                        mContext.errorListener()));
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //获取最后列表项的下标
                        lastItemPosition = mGridLayoutManager.findLastVisibleItemPosition();
                        //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                        mSwipeRefreshLayout.setEnabled(mGridLayoutManager
                                .findFirstCompletelyVisibleItemPosition() == 0);
                    }
                }
        );
    }

    /**
     * 下拉刷新事件监听
     */
    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mtvHint.setVisibility(View.VISIBLE);
                        action = I.ACTION_PULL_DOWN;
                        new DownloadCartListCountTask(mContext);
                    }
                }
        );
    }

    private void initData() {
        try {
            new DownloadCartListCountTask(mContext).execute();
            mGoodList = FuliCenterApplication.getInstance().getCartList();
            Log.e("main", "initData:mGoodlist:" + mGoodList.toString());
            sumPrice();
            if (mGoodList == null || mGoodList.size() == 0) {
                nothing.setVisibility(View.VISIBLE);
            } else {
                nothing.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getPath(){
        try {
            path = new ApiParams()
                    .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                    .with(I.PAGE_ID, "" + 0)
                    .with(I.PAGE_SIZE, "" + I.PAGE_SIZE_DEFAULT)
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
            Log.e("main", "CartFragment:" + path.toString());
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response.Listener<CartBean[]> responseDownloadNewGoodListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] newGoodBeen) {
                if(newGoodBeen!=null) {
                    mAdapter.setMore(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);

                    listSize = newGoodBeen.length;
                    //将数组转换为集合
                    list = Utils.array2List(newGoodBeen);
                    if (action == I.ACTION_DOWNLOAD || action == I.ACTION_PULL_DOWN) {
                        mAdapter.initItems(list);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addItems(list);
                    }
                    if(newGoodBeen.length< I.PAGE_SIZE_DEFAULT){
                        mAdapter.setMore(false);
                    }
                    for (CartBean cart : list) {
                        try {
                            path = new ApiParams()
                                    .with(I.CategoryGood.GOODS_ID,cart.getGoodsId() + "")
                                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
                            mContext.executeRequest(new GsonRequest<GoodDetailsBean>(path, GoodDetailsBean.class,
                                    responseDownloadGoodDetailListener(cart), mContext.errorListener()));
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
                    ArrayList<CartBean> cartList = mGoodList;
                    if (!cartList.contains(cart)) {
                        cartList.add(cart);
                    }
                }
                if (listSize == list.size()) {
                    initData();
                }
            }
        };
    }

    private void initView(View layout) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.sfl_cart);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.tv_refresh_hint);
        mGridLayoutManager = new LinearLayoutManager(mContext);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_cart);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mAdapter = new CartAdapter(mContext,mGoodList);
        mRecyclerView.setAdapter(mAdapter);
        mtvRankPrice = (TextView) layout.findViewById(R.id.tv_SumPrice);
        mtvSavePrice = (TextView) layout.findViewById(R.id.tvSavePrice);
        nothing = (TextView) layout.findViewById(R.id.tv_nothing);
    }

    private void sumPrice() {
        int sumPrice = 0;
        int currentPrice = 0;
        if (mGoodList != null || mGoodList.size() > 0) {
            for (CartBean cart : mGoodList) {
                GoodDetailsBean goods = cart.getGoods();
                if (goods != null && cart.isChecked()) {
                    sumPrice += convertPrice(goods.getRankPrice()) * cart.getCount();
                    currentPrice += convertPrice(goods.getCurrencyPrice()) * cart.getCount();
                }
            }
        }
        int savePrice = sumPrice - currentPrice;
        mtvRankPrice.setText("合计:￥" + sumPrice);
        mtvSavePrice.setText("节省:￥" + savePrice);
    }

    private int convertPrice(String rankPrice) {
        rankPrice = rankPrice.substring(rankPrice.indexOf("￥") + 1);
        int p1 = Integer.parseInt(rankPrice);
        return p1;
    }



}

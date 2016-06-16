package cn.ucai.fulicenter.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FulicenterActivity;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewGoodFrament extends Fragment {
    SwipeRefreshLayout sfl;
    RecyclerView rcv;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;

    FulicenterActivity mContext;
    GoodAdapter mAdapter;
    ArrayList<NewGoodBean> mGoodList;
    int pageId = 0;
    private int action = I.ACTION_DOWNLOAD;
    String path;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContext = (FulicenterActivity) getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_new_good, null);
        mGoodList = new ArrayList<NewGoodBean>();
        initView(layout);
        setListener();
        initData();
        Log.e("main", "NewGoodFrament");
        return layout;
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
    }

    private void setPullUpRefreshListener() {
        sfl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mtvHint.setVisibility(View.VISIBLE);
                pageId = 0;
                action = I.ACTION_PULL_DOWN;
                path = getPath(pageId);
                mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path, NewGoodBean[].class,
                        responseDownloadNewGoodListener(), mContext.errorListener()));
            }
        });
    }

    private void setPullDownRefreshListener() {
        rcv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastItemPosition;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        lastItemPosition == mAdapter.getItemCount() - 1) {
                    if (mAdapter.isMore()) {
                        sfl.setRefreshing(true);
                        action = I.ACTION_PULL_UP;
                        pageId += I.PAGE_SIZE_DEFAULT;
                        path = getPath(pageId);
                        mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,
                                NewGoodBean[].class, responseDownloadNewGoodListener(),
                                mContext.errorListener()));
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastItemPosition = mGridLayoutManager.findLastVisibleItemPosition();
                sfl.setEnabled(mGridLayoutManager
                .findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
    }

    private void initView(View layout) {
        sfl = (SwipeRefreshLayout) layout.findViewById(R.id.sfl_newgood);
        sfl.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.tv_refresh_hint);
        mGridLayoutManager = new GridLayoutManager(mContext, I.COLUM_NUM);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcv = (RecyclerView) layout.findViewById(R.id.rv_newgood);
        rcv.setHasFixedSize(true);
        rcv.setLayoutManager(mGridLayoutManager);
        mAdapter = new GoodAdapter(mContext, mGoodList);
        rcv.setAdapter(mAdapter);
    }


    private void initData() {
        try {
            Log.e("main", "initData");
            getPath(pageId);
            Log.e("main", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path, NewGoodBean[].class,
                    responseDownloadNewGoodListener(), mContext.errorListener()));
            Log.e("main", "mGoodList:" + mGoodList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getPath(int pageId) {
        try {
            String path = new ApiParams()
                    .with(I.NewAndBoutiqueGood.CAT_ID, I.CAT_ID + "")
                    .with(I.PAGE_ID, pageId + "")
                    .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                    .getRequestUrl(I.REQUEST_FIND_NEW_BOUTIQUE_GOODS);
            Log.e("main", "getPath:" + path.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("main", "getPath");
        return path;
    }

    private Response.Listener<NewGoodBean[]> responseDownloadNewGoodListener() {
        Log.e("main", "AAAAAAABBBBBBBBBBBBBBBBBBBB");
        return new Response.Listener<NewGoodBean[]>() {
            @Override
            public void onResponse(NewGoodBean[] newGoodBeans) {
                Log.e("main", "responseDownloadNewGoodListener");
                if (newGoodBeans != null) {
                    mAdapter.setMore(true);
                    sfl.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);
                    mAdapter.setFooterText(getResources().getString(R.string.load_more));
                    ArrayList<NewGoodBean> list = Utils.array2List(newGoodBeans);
                    if (action == I.ACTION_DOWNLOAD) {
                        mAdapter.initItems(list);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addItem(list);
                    }
                    if (newGoodBeans.length < I.PAGE_SIZE_DEFAULT) {
                        mAdapter.setMore(false);
                        mAdapter.setFooterText(getResources().getString(R.string.no_more));
                    }
                }
            }
        };
    }
}

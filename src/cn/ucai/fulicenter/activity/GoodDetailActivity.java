package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.DisplayUtils;
import cn.ucai.fulicenter.view.FlowIndicator;
import cn.ucai.fulicenter.view.SlideAutoLoopView;

public class GoodDetailActivity extends BaseActivity {
    Context mContext;
    GoodDetailsBean mGood;

    TextView tv_englishName, tv_name;
    TextView tv_Price, tv_ShopPrice;
    WebView wv_Brief;

    ImageView mivCollect, mivAddCart, mivShare;
    TextView mtvCartCount;
    SlideAutoLoopView mSlideAutoLoopView;
    FlowIndicator mFlowIndicator;
    LinearLayout mLayoutColors;
    int goodId;

    boolean isCollect;

    int mCurrentColor;

    int actionCollect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_details);
        mContext = this;
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        setCollectListener();
    }

    private void setCollectListener() {
        mivCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = FuliCenterApplication.getInstance().getUser();
                String mUserName = user.getMUserName();
                String path = null;
                if (isCollect) {
                    actionCollect = 1;
                    try {
                        path = new ApiParams()
                                .with(I.Collect.USER_NAME, mUserName)
                                .with(I.Collect.GOODS_ID, goodId + "")
                                .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    actionCollect = 2;
                    try {
                        path = new ApiParams()
                                .with(I.Collect.USER_NAME, mUserName)
                                .with(I.Collect.GOODS_ID, goodId + "")
                                .with(I.Collect.GOODS_NAME, mGood.getGoodsName())
                                .with(I.Collect.GOODS_ENGLISH_NAME, mGood.getGoodsEnglishName())
                                .with(I.Collect.GOODS_THUMB, mGood.getGoodsThumb())
                                .with(I.Collect.GOODS_IMG, mGood.getGoodsImg())
                                .with(I.Collect.ADD_TIME, mGood.getAddTime() + "")
                                .getRequestUrl(I.REQUEST_ADD_COLLECT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                        responseSetCollectListener(), errorListener()));
            }
        });
    }

    private Response.Listener<MessageBean> responseSetCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    if (actionCollect == 2) {
                        mivCollect.setImageResource(R.drawable.bg_collect_out);
                        isCollect = true;
                    } else {
                        mivCollect.setImageResource(R.drawable.bg_collect_in);
                        isCollect = false;
                    }
                    new DownloadCollectCountTask(mContext).execute();
                    Utils.showToast(mContext, messageBean.getMsg(), Toast.LENGTH_SHORT);
                }
            }
        };
    }




    private void initData() {
        goodId = getIntent().getIntExtra(D.NewGood.KEY_GOODS_ID, 0);
        try {
            String path = new ApiParams()
                    .with(D.NewGood.KEY_GOODS_ID, goodId + "")
                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
            executeRequest(new GsonRequest<GoodDetailsBean>(path, GoodDetailsBean.class,
                    responseDownloadGoodDetailsListener(), errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetailsListener() {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                if (goodDetailsBean != null) {
                    mGood = goodDetailsBean;
                    DisplayUtils.initBackWithTitle(GoodDetailActivity.this, getResources().getString(R.string.title_good_details));
                    tv_englishName.setText(mGood.getGoodsEnglishName());
                    tv_name.setText(mGood.getGoodsName());
                    tv_Price.setText(mGood.getCurrencyPrice());
                    wv_Brief.loadDataWithBaseURL(null, mGood.getGoodsBrief().trim(), D.TEXT_HTML, D.UTF_8, null);

                    initColorsBanner();
                } else {
                    Utils.showToast(mContext, "商品详情下载失败", Toast.LENGTH_LONG);
                    finish();
                }
            }
        };
    }

    private void initColorsBanner() {
        upDateColor(0);
        for (int i = 0; i < mGood.getProperties().length; i++) {
            mCurrentColor = 1;
            View inflate = View.inflate(mContext, R.layout.layout_property_color, null);
            final NetworkImageView ivColor = (NetworkImageView) inflate.findViewById(R.id.ivColorItem);
            String colorImg = mGood.getProperties()[i].getColorImg();
            if (colorImg.isEmpty()) {
                continue;
            }
            ImageUtils.setGoodDetailThumb(colorImg, ivColor);
            mLayoutColors.addView(inflate);
            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upDateColor(mCurrentColor);
                }
            });
        }
    }

    private void upDateColor(int i) {
        AlbumBean[] albums = mGood.getProperties()[i].getAlbums();
        String[] albumImgUrl = new String[albums.length];
        for (int j = 0; j < albumImgUrl.length; j++) {
            albumImgUrl[j] = albums[j].getImgUrl();
        }
        mSlideAutoLoopView.startPlayLoop(mFlowIndicator, albumImgUrl, albumImgUrl.length);
    }

    private void initView() {
        mivCollect = (ImageView) findViewById(R.id.ivCollect);
        mivAddCart = (ImageView) findViewById(R.id.ivAddCart);
        mivShare = (ImageView) findViewById(R.id.ivShare);
        mtvCartCount = (TextView) findViewById(R.id.tvCartCount);

        mSlideAutoLoopView = (SlideAutoLoopView) findViewById(R.id.salv);
        mFlowIndicator = (FlowIndicator) findViewById(R.id.indicator);
        mLayoutColors = (LinearLayout) findViewById(R.id.layoutColorSelector);

        tv_englishName = (TextView) findViewById(R.id.tvGoodEnglishName);
        tv_name = (TextView) findViewById(R.id.tvGoodName);
        tv_ShopPrice = (TextView) findViewById(R.id.tvShopPrice);
        tv_Price = (TextView) findViewById(R.id.tvCurrencyPrice);
        wv_Brief = (WebView) findViewById(R.id.wvGoodBrief);
        WebSettings settings = wv_Brief.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
        DisplayUtils.initBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCollectStatus();
    }

    private void initCollectStatus() {
        User user = FuliCenterApplication.getInstance().getUser();
        if (user != null) {
            try {
                String path = new ApiParams()
                        .with(I.Collect.USER_NAME, user.getMUserName())
                        .with(I.Collect.GOODS_ID, goodId + "")
                        .getRequestUrl(I.REQUEST_IS_COLLECT);
                Log.e("main", "initCollectStatus" + path.toString());
                executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                        responseIsCollectListener(), errorListener()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            mivCollect.setImageResource(R.drawable.bg_collect_in);
            isCollect = false;
        }
    }

    private Response.Listener<MessageBean> responseIsCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    isCollect = true;
                    mivCollect.setImageResource(R.drawable.bg_collect_out);
                } else {
                    isCollect = false;
                    mivCollect.setImageResource(R.drawable.bg_collect_in);
                }
            }
        };
    }
}

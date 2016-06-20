package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;

/**
 * Created by Administrator on 2016/6/20.
 */
public class DownloadCollectCountTask extends BaseActivity{
    Context mContext;
    String userName;

    String path;

    public DownloadCollectCountTask(Context mContext) {
        this.mContext = mContext;
        this.userName = FuliCenterApplication.getInstance().getUserName();
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Cart.USER_NAME,userName)
                    .getRequestUrl(I.REQUEST_FIND_COLLECT_COUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                responseDownloadCollectCountTaskLiskListener(), errorListener()));
    }

    private Response.Listener<MessageBean> responseDownloadCollectCountTaskLiskListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    FuliCenterApplication.getInstance().setmCollectCount(Integer.parseInt(messageBean.getMsg()));
                }
                mContext.sendStickyBroadcast(new Intent("update_collect_count"));
            }
        };
    }
}

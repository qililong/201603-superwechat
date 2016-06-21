package cn.ucai.fulicenter.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.CollectActivity;
import cn.ucai.fulicenter.activity.GoodDetailActivity;
import cn.ucai.fulicenter.bean.CollectBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.view.DisplayUtils;
import cn.ucai.fulicenter.view.FooterViewHolder;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by clawpo on 16/6/15.
 */
public class CollectAdapter extends RecyclerView.Adapter<ViewHolder> {
    CollectActivity mContext;
    ArrayList<CollectBean> mGoodList;

    CollectItemViewHolder goodHolder;
    FooterViewHolder footerHolder;

    private String footerText;
    private boolean isMore;



    public void setFooterText(String footerText) {
        this.footerText = footerText;
        notifyDataSetChanged();
    }

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public CollectAdapter(CollectActivity mContext, ArrayList<CollectBean> mGoodList) {
        this.mContext = mContext;
        this.mGoodList = mGoodList;

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder = null;
        switch (viewType){
            case I.TYPE_ITEM:
                holder = new CollectItemViewHolder(inflater.inflate(R.layout.item_collect,parent,false));
                break;
            case I.TYPE_FOOTER:
                Log.e("main", "TYPE_FOOTER");
                holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer,parent,false));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder instanceof FooterViewHolder){
            footerHolder = (FooterViewHolder) holder;
            footerHolder.tvFooter.setText(footerText);
            footerHolder.tvFooter.setVisibility(View.VISIBLE);
        }
        if(holder instanceof CollectItemViewHolder){
            goodHolder = (CollectItemViewHolder) holder;
            final CollectBean good = mGoodList.get(position);
            goodHolder.tvGoodName.setText(good.getGoodsName());
            ImageUtils.setNewGoodThumb(good.getGoodsThumb(), goodHolder.nivThumb);
            goodHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String path = new ApiParams()
                                .with(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUserName())
                                .with(I.Collect.GOODS_ID, good.getGoodsId() + "")
                                .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                        mContext.executeRequest(new GsonRequest<MessageBean>(path, MessageBean.class,
                                responseDelCollectListener(good), mContext.errorListener()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            goodHolder.layoutGood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, GoodDetailActivity.class)
                            .putExtra(D.NewGood.KEY_GOODS_ID,good.getGoodsId()));
                }
            });
        }

    }

    private Response.Listener<MessageBean> responseDelCollectListener(final CollectBean good) {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()) {
                    mGoodList.remove(good);
                    notifyDataSetChanged();
                    new DownloadCollectCountTask(mContext).execute();
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return mGoodList==null?1:mGoodList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==getItemCount()-1){
            Log.e("main", "getItemViewType");
            return I.TYPE_FOOTER;
        }else {
            return I.TYPE_ITEM;
        }
    }

    public void initItems(ArrayList<CollectBean> list) {
        if(mGoodList!=null && !mGoodList.isEmpty()){
            mGoodList.clear();
        }
        mGoodList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<CollectBean> list) {
        mGoodList.addAll(list);
        notifyDataSetChanged();
    }



    class CollectItemViewHolder extends ViewHolder {
        LinearLayout layoutGood;
        NetworkImageView nivThumb;
        TextView tvGoodName;
        ImageView delete;

        public CollectItemViewHolder(View itemView) {
            super(itemView);
            layoutGood = (LinearLayout) itemView.findViewById(R.id.layout_collect);
            nivThumb = (NetworkImageView) itemView.findViewById(R.id.niv_collect_thumb);
            tvGoodName = (TextView) itemView.findViewById(R.id.tv_collect);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            DisplayUtils.initBackWithTitle(mContext,"我的收藏");
        }
    }
}

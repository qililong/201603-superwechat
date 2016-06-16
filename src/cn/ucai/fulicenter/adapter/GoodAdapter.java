package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.view.FooterViewHolder;

/**
 * Created by Administrator on 2016/6/15.
 */
public class GoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Context mContext;
    ArrayList<NewGoodBean> goodsList;

    GoodItemViewHolder goodHolder;
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
    public GoodAdapter(Context mContext, ArrayList<NewGoodBean> goodsList) {
        this.mContext = mContext;
        this.goodsList = goodsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case I.TYPE_ITEM:
                holder = new GoodItemViewHolder(inflater.inflate(R.layout.item_new_good, parent, false));
                break;
            case I.TYPE_FOOTER:
                holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer, parent, false));
                break;
        }
        return holder;
    }

    @Override
    public int getItemCount() {
        return goodsList == null?0:goodsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return I.TYPE_FOOTER;
        } else {
            return I.TYPE_ITEM;
        }
    }

    public void initItems(ArrayList<NewGoodBean> list) {
        if (goodsList != null) {
            goodsList.clear();
        }
        goodsList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<NewGoodBean> list) {
        for (NewGoodBean ng : list) {
            if (!goodsList.contains(ng)) {
                goodsList.add(ng);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            footerHolder = (FooterViewHolder) holder;
            footerHolder.tvFooter.setText(footerText);
            footerHolder.tvFooter.setVisibility(View.VISIBLE);
        }
        if (holder instanceof GoodItemViewHolder) {
            goodHolder = (GoodItemViewHolder) holder;
            final NewGoodBean good = goodsList.get(position);
            goodHolder.tv_name.setText(good.getGoodsName());
            goodHolder.tv_price.setText(good.getCurrencyPrice());
            ImageUtils.setNewGoodThumb(good.getGoodsThumb(), goodHolder.iv_thumb);
        }
    }

    public class GoodItemViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutGood;
        NetworkImageView iv_thumb;
        TextView tv_name;
        TextView tv_price;

        public GoodItemViewHolder (View itemView) {
            super(itemView);
            layoutGood = (LinearLayout) itemView.findViewById(R.id.layout_good);
            iv_thumb = (NetworkImageView) itemView.findViewById(R.id.niv_good_thumb);
            tv_name = (TextView) itemView.findViewById(R.id.tv_good_name);
            tv_price = (TextView) itemView.findViewById(R.id.tv_good_price);
        }
    }
}

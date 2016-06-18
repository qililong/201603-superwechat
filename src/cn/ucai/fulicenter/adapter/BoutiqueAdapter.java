package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.GoodDetailActivity;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.view.FooterViewHolder;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by clawpo on 16/6/15.
 */
public class BoutiqueAdapter extends RecyclerView.Adapter<ViewHolder> {
    Context mContext;
    ArrayList<BoutiqueBean> mBoutiqueBeanList;

    BoutiqueItemViewHolder boutiqueHolder;
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

    public BoutiqueAdapter(Context mContext, ArrayList<BoutiqueBean> mGoodList) {
        this.mContext = mContext;
        this.mBoutiqueBeanList = mGoodList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder = null;
        switch (viewType){
            case I.TYPE_ITEM:
                holder = new BoutiqueItemViewHolder(inflater.inflate(R.layout.item_boutique,parent,false));
                break;
            case I.TYPE_FOOTER:
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
        if(holder instanceof BoutiqueItemViewHolder){
            boutiqueHolder = (BoutiqueItemViewHolder) holder;
            final BoutiqueBean good = mBoutiqueBeanList.get(position);
            boutiqueHolder.tv_Name.setText(good.getName());
            boutiqueHolder.tv_description.setText(good.getDescription());
            boutiqueHolder.tv_title.setText(good.getTitle());
            ImageUtils.setNewGoodThumb(good.getImageurl(),boutiqueHolder.nivThumb);

            boutiqueHolder.layoutGood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, GoodDetailActivity.class)
                            .putExtra(D.Boutique.KEY_GOODS_ID,good.getId()));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        Log.e("main", "getItemCount" + mBoutiqueBeanList.size());
        return mBoutiqueBeanList == null ? 1 : mBoutiqueBeanList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==getItemCount()-1){
            return I.TYPE_FOOTER;
        }else{
            return I.TYPE_ITEM;
        }
    }

    public void initItems(ArrayList<BoutiqueBean> list) {
        if(mBoutiqueBeanList!=null && !mBoutiqueBeanList.isEmpty()){
            mBoutiqueBeanList.clear();
        }
        mBoutiqueBeanList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<BoutiqueBean> list) {
        mBoutiqueBeanList.addAll(list);
        notifyDataSetChanged();
    }



    class BoutiqueItemViewHolder extends ViewHolder {
        RelativeLayout layoutGood;
        NetworkImageView nivThumb;
        TextView tv_Name;
        TextView tv_description;
        TextView tv_title;

        public BoutiqueItemViewHolder(View itemView) {
            super(itemView);
            layoutGood = (RelativeLayout) itemView.findViewById(R.id.layout_boutique);
            nivThumb = (NetworkImageView) itemView.findViewById(R.id.niv_boutique_thumb);
            tv_Name = (TextView) itemView.findViewById(R.id.tv_boutique_name);
            tv_description = (TextView) itemView.findViewById(R.id.tv_boutique_description);
            tv_title = (TextView) itemView.findViewById(R.id.tv_boutique_title);

        }
    }
}

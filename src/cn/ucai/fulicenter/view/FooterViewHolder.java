package cn.ucai.fulicenter.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

/**
 * Created by Administrator on 2016/6/15.
 */
public class FooterViewHolder extends RecyclerView.ViewHolder{
    public TextView tvFooter;

    public FooterViewHolder(View view) {
        super(view);
        tvFooter = (TextView) view.findViewById(R.id.tv_footer);
    }
}

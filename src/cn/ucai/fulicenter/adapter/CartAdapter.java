package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by clawpo on 16/6/15.
 */
public class CartAdapter extends RecyclerView.Adapter<ViewHolder> {
    Context mContext;
    ArrayList<CartBean> mCartBeanList;

    CartItemViewHolder cartHolder;
    private boolean isMore;


    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public CartAdapter(Context mContext, ArrayList<CartBean> mGoodList) {
        this.mContext = mContext;
        this.mCartBeanList = mGoodList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder = null;
        holder = new CartItemViewHolder(inflater.inflate(R.layout.item_cart,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cartHolder = (CartItemViewHolder) holder;
        final CartBean cart = mCartBeanList.get(position);
        GoodDetailsBean good = cart.getGoods();
        cartHolder.tv_Name.setText(good.getGoodsName());
        cartHolder.tvGoodsPrice.setText(good.getRankPrice());
        cartHolder.tvCartCount.setText("" + cart.getCount());
        ImageUtils.setNewGoodThumb(good.getGoodsThumb(), cartHolder.nivThumb);
        AddDelCartClickLinstener listener = new AddDelCartClickLinstener(good, cartHolder, cart);
        cartHolder.ivAddCart.setOnClickListener(listener);
        cartHolder.ivReduceCart.setOnClickListener(listener);
        cartHolder.chkSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cart.setChecked(!cart.isChecked());
                cartHolder.chkSelect.setChecked(!cartHolder.chkSelect.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCartBeanList == null ? 0 : mCartBeanList.size();
    }


    public void initItems(ArrayList<CartBean> list) {
        if(mCartBeanList!=null && !mCartBeanList.isEmpty()){
            mCartBeanList.clear();
        }
        mCartBeanList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<CartBean> list) {
        mCartBeanList.addAll(list);
        notifyDataSetChanged();
    }



    class CartItemViewHolder extends ViewHolder {
        CheckBox chkSelect;
        NetworkImageView nivThumb;
        TextView tv_Name;
        TextView tvGoodsPrice;
        TextView tvCartCount;
        ImageView ivAddCart;
        ImageView ivReduceCart;

        public CartItemViewHolder(View itemView) {
            super(itemView);
            chkSelect = (CheckBox) itemView.findViewById(R.id.chkSelect);
            nivThumb = (NetworkImageView) itemView.findViewById(R.id.ivGoodsThumb);
            tv_Name = (TextView) itemView.findViewById(R.id.tvGoodsName);
            tvGoodsPrice = (TextView) itemView.findViewById(R.id.tvGoodsPrice);
            tvCartCount = (TextView) itemView.findViewById(R.id.tvCartCount);
            ivAddCart = (ImageView) itemView.findViewById(R.id.ivAddCart);
            ivReduceCart = (ImageView) itemView.findViewById(R.id.ivReduceCart);
        }
    }

    class AddDelCartClickLinstener implements View.OnClickListener {

        GoodDetailsBean good;
        CartItemViewHolder holder;
        CartBean cart;

        public AddDelCartClickLinstener(GoodDetailsBean good, CartItemViewHolder holder, CartBean cart) {
            this.good = good;
            this.holder = holder;
            this.cart = cart;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ivAddCart:
                    Utils.add(mContext, good);
                    holder.tvCartCount.setText(cart.getCount()+"");
                    break;
                case R.id.ivReduceCart:
                    Utils.delCart(mContext, good);
                    holder.tvCartCount.setText(cart.getCount()+"");
                    break;
            }
        }
    }
}

package cn.ucai.fulicenter.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.UserUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class Personal_CenterFragment extends Fragment {

    Context mContext;

    NetworkImageView mivUserAvatar;
    TextView mtvUserName;
    TextView mtvCollectCount;
    TextView mtvSettings;
    ImageView mivMessage;
    LinearLayout mLayoutCenterCollet;
    RelativeLayout mLayoutCenterUserInfo;

    int mCollectCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext =  this.getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_personal__center, null);
        initView(layout);
        initData();
        return layout;
    }

    private void initData() {
        mCollectCount = FuliCenterApplication.getInstance().getmCollectCount();
        mtvCollectCount.setText("" + mCollectCount);
        if (FuliCenterApplication.getInstance().getUser() != null) {
            UserUtils.setCurrentUserAvatar(mivUserAvatar);
            UserUtils.setCurrentUserBeanNick(mtvUserName);
        }

    }

    private void initView(View layout) {
        mivUserAvatar = (NetworkImageView) layout.findViewById(R.id.iv_user_avatar);
        mtvUserName = (TextView) layout.findViewById(R.id.tv_user_name);
        mLayoutCenterCollet = (LinearLayout) layout.findViewById(R.id.layout_center_collect);
        mtvCollectCount = (TextView) layout.findViewById(R.id.tv_collect_count);
        mtvSettings = (TextView) layout.findViewById(R.id.tv_center_settings);
        mivMessage = (ImageView) layout.findViewById(R.id.iv_persona_center_msg);
        mLayoutCenterUserInfo = (RelativeLayout) layout.findViewById(R.id.center_user_info);

        initOrderList(layout);
    }

    private void initOrderList(View layout) {
        GridView mOrderList = (GridView) layout.findViewById(R.id.center__user_order_list);
        ArrayList<HashMap<String, Integer>> imagelist = new ArrayList<HashMap<String, Integer>>();

        HashMap<String, Integer> map1 = new HashMap<String, Integer>();
        map1.put("image", R.drawable.order_list1);
        imagelist.add(map1);
        HashMap<String, Integer> map2 = new HashMap<String, Integer>();
        map2.put("image", R.drawable.order_list2);
        imagelist.add(map1);
        HashMap<String, Integer> map3 = new HashMap<String, Integer>();
        map3.put("image", R.drawable.order_list3);
        imagelist.add(map1);
        HashMap<String, Integer> map4 = new HashMap<String, Integer>();
        map4.put("image", R.drawable.order_list4);
        imagelist.add(map1);
        HashMap<String, Integer> map5 = new HashMap<String, Integer>();
        map5.put("image", R.drawable.order_list5);
        imagelist.add(map1);

        SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, imagelist, R.layout.simple_grid_item, new String[]{"image"},
                new int[]{R.id.image});
        mOrderList.setAdapter(simpleAdapter);
    }

    class UpdateUserChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            new DownloadCollectCountTask(mContext).execute();
            initData();
        }
    }

    UpdateUserChangedReceiver mUpdateUserChangedReceiver;

    private void registerUpdateUserChangedReceiver() {
        mUpdateUserChangedReceiver = new UpdateUserChangedReceiver();
        IntentFilter filter = new IntentFilter("update_user");
        mContext.registerReceiver(mUpdateUserChangedReceiver, filter);
    }

    class UpdateCollectCountReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    }

    UpdateCollectCountReceiver mUpdateCollectCountReceiver;

    private void registerUpdateCollectCountReceiver() {
        mUpdateCollectCountReceiver = new UpdateCollectCountReceiver();
        IntentFilter filter = new IntentFilter("update_collect_count");
        mContext.registerReceiver(mUpdateCollectCountReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUpdateCollectCountReceiver != null) {
            mContext.unregisterReceiver(mUpdateCollectCountReceiver);
        }
        if (mUpdateUserChangedReceiver != null) {
            mContext.unregisterReceiver(mUpdateUserChangedReceiver);
        }
    }
}

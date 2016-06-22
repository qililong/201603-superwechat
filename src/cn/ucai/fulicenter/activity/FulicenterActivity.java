package cn.ucai.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.fragment.BoutiqueFragment;
import cn.ucai.fulicenter.fragment.CartFragment;
import cn.ucai.fulicenter.fragment.CategoryFragment;
import cn.ucai.fulicenter.fragment.NewGoodFragment;
import cn.ucai.fulicenter.fragment.Personal_CenterFragment;

public class FulicenterActivity extends BaseActivity {

    RadioButton mNewGood;
    RadioButton mBoutique;
    RadioButton mCategory;
    RadioButton mCart;
    RadioButton mPersonal;

    RadioButton[] radios = new RadioButton[5];
    NewGoodFragment mNewGoodFragment;
    BoutiqueFragment mBoutiqueFragment;
    CategoryFragment mCategoryFragment;
    CartFragment mCartFragment;
    Personal_CenterFragment mPersonal_CenterFragment;
    Fragment[] mFragment = new Fragment[5];
    private int index;

    private int currentTabIndex;
    Context mContext;

    TextView tvCartCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulicenter);
        mContext = this;
        initView();
        init();
        initFragment();
        registerCartCountChangeListener();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container1, mCategoryFragment).hide(mCategoryFragment)
                .add(R.id.fragment_container1, mBoutiqueFragment).hide(mBoutiqueFragment)
                .add(R.id.fragment_container1, mNewGoodFragment)
//                .hide(contactListFragment)
                .show(mNewGoodFragment)
                .commit();
    }

    private void init() {
        if (FuliCenterApplication.getInstance().getUser() != null &&
                FuliCenterApplication.getInstance().getCartList().size() > 0) {
            tvCartCount.setText("" + FuliCenterApplication.getInstance().getCartList().size());
            tvCartCount.setVisibility(View.VISIBLE);
        } else {
            tvCartCount.setVisibility(View.GONE);
        }
    }

    private void initFragment() {
        mNewGoodFragment = new NewGoodFragment();
        mBoutiqueFragment = new BoutiqueFragment();
        mCategoryFragment = new CategoryFragment();
        mCartFragment = new CartFragment();
        mPersonal_CenterFragment = new Personal_CenterFragment();
        mFragment[0] = mNewGoodFragment;
        mFragment[1] = mBoutiqueFragment;
        mFragment[2] = mCategoryFragment;
        mFragment[3] = mCartFragment;
        mFragment[4] = mPersonal_CenterFragment;
    }

    private void initView() {
        mNewGood = (RadioButton) findViewById(R.id.btn_new_good);
        mBoutique = (RadioButton) findViewById(R.id.btn_boutique);
        mCategory = (RadioButton) findViewById(R.id.btn_category);
        mCart = (RadioButton) findViewById(R.id.btn_cart);
        mPersonal = (RadioButton) findViewById(R.id.btn_personal);
        tvCartCount = (TextView) findViewById(R.id.tvCartHint);

        radios[0] = mNewGood;
        radios[1] = mBoutique;
        radios[2] = mCategory;
        radios[3] = mCart;
        radios[4] = mPersonal;
    }

    public void onCheckedChange(View view) {
        switch (view.getId()) {
            case R.id.btn_new_good:
                index = 0;
                break;
            case R.id.btn_boutique:
                index = 1;
                break;
            case R.id.btn_category:
                index = 2;
                break;
            case R.id.btn_cart:
                if (FuliCenterApplication.getInstance().getUser() == null) {
                    mContext.startActivity(new Intent(FulicenterActivity.this, LoginActivity.class)
                            .putExtra("action", "cart"));
                } else {
                    index = 3;
                }
                break;
            case R.id.btn_personal:
                if (FuliCenterApplication.getInstance().getUser() == null) {
                    mContext.startActivity(new Intent(FulicenterActivity.this, LoginActivity.class)
                    .putExtra("action","personal"));
                } else {
                    index = 4;
                }
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(mFragment[currentTabIndex]);
            if (!mFragment[index].isAdded()) {
                trx.add(R.id.fragment_container1, mFragment[index]);
            }
            trx.show(mFragment[index]).commit();
            setVisibles(index);
            currentTabIndex = index;
            FuliCenterApplication.getInstance().setIsNow(index);
        }
    }

    public void setVisibles(int index) {
        for (int i = 0; i < radios.length; i++) {
            if (i == index) {
                radios[i].setChecked(true);
            } else {
                radios[i].setChecked(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getStringExtra("action") != null) {

            Log.e("main", "action:" + getIntent().getStringExtra("action").toString());
        }
        if (FuliCenterApplication.getInstance().getUser() != null && getIntent().getStringExtra("action") != null) {
            if (getIntent().getStringExtra("action").equals("personal")) {
                index = 4;
            }
        } else {
            radios[index].setChecked(true);
        }
        if (FuliCenterApplication.getInstance().getUser() == null && index == 4) {
            index = 0;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(mFragment[currentTabIndex]);
            if (!mFragment[index].isAdded()) {
                trx.add(R.id.fragment_container1, mFragment[index]);
            }
            trx.show(mFragment[index]).commit();
            setVisibles(index);
            currentTabIndex = index;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    class CartCountBeceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            init();
        }
    }

    CartCountBeceiver mCartCountBeceiver;

    private void registerCartCountChangeListener() {
        mCartCountBeceiver = new CartCountBeceiver();
        IntentFilter filter = new IntentFilter("update_cart_list");
        registerReceiver(mCartCountBeceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCartCountBeceiver != null) {
            unregisterReceiver(mCartCountBeceiver);
        }
    }
}

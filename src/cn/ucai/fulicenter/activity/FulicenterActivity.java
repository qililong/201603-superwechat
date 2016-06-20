package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;

import cn.ucai.fulicenter.FuliCenterApplication;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.fragment.BoutiqueFragment;
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
    Personal_CenterFragment mPersonal_CenterFragment;
    Fragment[] mFragment = new Fragment[5];
    private int index;

    private int currentTabIndex;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulicenter);
        mContext = this;
        initView();
        initFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container1, mCategoryFragment).hide(mCategoryFragment)
                .add(R.id.fragment_container1, mBoutiqueFragment).hide(mBoutiqueFragment)
                .add(R.id.fragment_container1, mNewGoodFragment)
//                .hide(contactListFragment)
                .show(mNewGoodFragment)
                .commit();
    }

    private void initFragment() {
        mNewGoodFragment = new NewGoodFragment();
        mBoutiqueFragment = new BoutiqueFragment();
        mCategoryFragment = new CategoryFragment();
        mPersonal_CenterFragment = new Personal_CenterFragment();
        mFragment[0] = mNewGoodFragment;
        mFragment[1] = mBoutiqueFragment;
        mFragment[2] = mCategoryFragment;
        mFragment[4] = mPersonal_CenterFragment;
    }

    private void initView() {
        mNewGood = (RadioButton) findViewById(R.id.btn_new_good);
        mBoutique = (RadioButton) findViewById(R.id.btn_boutique);
        mCategory = (RadioButton) findViewById(R.id.btn_category);
        mCart = (RadioButton) findViewById(R.id.btn_cart);
        mPersonal = (RadioButton) findViewById(R.id.btn_personal);

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
                index = 3;
                break;
            case R.id.btn_personal:
                if (FuliCenterApplication.getInstance().getUser() == null) {
                    mContext.startActivity(new Intent(FulicenterActivity.this,LoginActivity.class));
                }
                index = 4;
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
        }
    }

    public void setVisibles(int index) {
        for (int i = 0; i < radios.length; i++) {
            if (i == index) {
                radios[i].setSelected(true);
            } else {
                radios[i].setSelected(false);
            }
        }
    }
}

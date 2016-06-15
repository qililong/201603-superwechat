package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import cn.ucai.fulicenter.R;

public class FulicenterActivity extends Activity {

    RadioButton mNewGood;
    RadioButton mBoutique;
    RadioButton mCategory;
    RadioButton mCart;
    RadioButton mPersonal;

    RadioButton[] radios = new RadioButton[5];
    private int index;

    private int currentTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulicenter);
        initView();
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
                index = 4;
                break;
        }
        currentTabIndex = index;
        setVisibles(index);
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

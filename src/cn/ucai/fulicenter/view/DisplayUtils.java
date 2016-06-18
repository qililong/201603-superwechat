package cn.ucai.fulicenter.view;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

/**
 * Created by Administrator on 2016/6/18.
 */
public class DisplayUtils {

    public static void initBack(final Activity activity) {
        View clickArea = activity.findViewById(R.id.backClickArea);
        clickArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    public static void initBackWithTitle(final Activity activity, String title) {
        TextView viewById = (TextView) activity.findViewById(R.id.tv_head_title);
        viewById.setText(title);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}

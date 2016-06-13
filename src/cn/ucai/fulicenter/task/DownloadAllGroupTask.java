package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Group;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.superWeChatApplication;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadAllGroupTask extends BaseActivity {
    private static final String TAG = DownloadAllGroupTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadAllGroupTask(Context context, String username) {
        this.mContext = context;
        this.username = username;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path, Group[].class,
                responseDownloadGroupListTaskLiskListener(), errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadGroupListTaskLiskListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] contacts) {
                if (contacts != null) {
                    ArrayList<Group> contactList =
                            superWeChatApplication.getInstance().getGroupList();
                    ArrayList<Group> list = Utils.array2List(contacts);
                    contactList.clear();
                    contactList.addAll(list);
                    superWeChatApplication.getInstance().setGroupList(contactList);
                    Log.i("main", "responseDownloadGroupListTaskLiskListener" + contactList.size());
                }
                mContext.sendStickyBroadcast(new Intent("update_group_list"));
            }
        };
    }
}

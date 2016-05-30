package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.superWeChatApplication;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadPublicGroupTask extends BaseActivity {
    private static final String TAG = DownloadPublicGroupTask.class.getName();
    Context mContext;
    String username;
    String path;
    int pageId, pageSize;

    public DownloadPublicGroupTask(Context mContext, String username, int pageId, int pageSize) {
        this.mContext = mContext;
        this.username = username;
        this.pageId = pageId;
        this.pageSize = pageSize;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME,username)
                    .with(I.PAGE_ID,I.PAGE_ID_DEFAULT+"")
                    .with(I.PAGE_SIZE,I.PAGE_SIZE_DEFAULT+"")
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUPS);
            Log.i("main", path.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path, Group[].class,
                responseDownloadConractListTaskLiskListener(), errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadConractListTaskLiskListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] contacts) {
                if (contacts != null) {
                    Log.i("main", "responseDownloadConractListTaskLiskListener,contacts:" + contacts.length);
                    ArrayList<Group> contactList =
                            superWeChatApplication.getInstance().getPublicGroupList();
                    ArrayList<Group> list = Utils.array2List(contacts);
                    for (Group g : list) {
                        if (!contactList.contains(g)) {
                            contactList.add(g);
                        }
                    }
                    mContext.sendStickyBroadcast(new Intent("update_public_group"));
                }
            }
        };
    }
}

package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Contact;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.superWeChatApplication;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadContactListTask extends BaseActivity {
    private static final String TAG = DownloadContactListTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadContactListTask(Context mContext, String username) {
        this.mContext = mContext;
        this.username = username;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Contact[]>(path, Contact[].class,
                responseDownloadConractListTaskLiskListener(), errorListener()));
    }

    private Response.Listener<Contact[]> responseDownloadConractListTaskLiskListener() {
        return new Response.Listener<Contact[]>() {
            @Override
            public void onResponse(Contact[] contacts) {
                if (contacts != null) {
                    ArrayList<Contact> contactList =
                            superWeChatApplication.getInstance().getContactList();
                    ArrayList<Contact> list = Utils.array2List(contacts);
                    contactList.clear();
                    contactList.addAll(list);
                    HashMap<String, Contact> userList =
                            superWeChatApplication.getInstance().getUserList();
                    userList.clear();
                    for (Contact c : list) {
                        userList.put(c.getMContactCname(), c);
                    }
                    superWeChatApplication.getInstance().setUserList(userList);
                }
                mContext.sendStickyBroadcast(new Intent("update_contact_list"));
            }
        };
    }
}

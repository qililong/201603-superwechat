package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Member;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.superWeChatApplication;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/31.
 */
public class DownloadGroupMemberTask extends BaseActivity{
    private static final String TAG = DownloadGroupMemberTask.class.getName();
    Context mContext;
    String hxid;
    String path;

    public DownloadGroupMemberTask(Context context, String hxid) {
        this.mContext = context;
        this.hxid = hxid;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Member.GROUP_HX_ID,hxid)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Member[]>(path, Member[].class,
                responseDownloadGroupListTaskLiskListener(), errorListener()));
    }

    private Response.Listener<Member[]> responseDownloadGroupListTaskLiskListener() {
        return new Response.Listener<Member[]>() {
            @Override
            public void onResponse(Member[] members) {
                if (members != null) {
                    HashMap<String, ArrayList<Member>> groupMembers = superWeChatApplication.getInstance().getGroupMembers();
                    ArrayList<Member> members1 = Utils.array2List(members);
                    ArrayList<Member> members2 = groupMembers.get(hxid);
                    if (members2 != null) {
                        members2.clear();
                        members2.addAll(members1);
                    }
                } else {
                    return;
                }
                mContext.sendStickyBroadcast(new Intent("update_member_list"));
            }
        };
    }
}

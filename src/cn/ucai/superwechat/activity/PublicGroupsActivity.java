/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ucai.superwechat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.superWeChatApplication;
import cn.ucai.superwechat.task.DownloadPublicGroupTask;
import cn.ucai.superwechat.utils.UserUtils;

public class PublicGroupsActivity extends BaseActivity {
	private ProgressBar pb;
	private ListView listView;
	private GroupsAdapter adapter;
	
	private ArrayList<Group> groupsList;
	private boolean isLoading;
	private boolean isFirstLoading = true;
	private boolean hasMoreData = true;
	private String cursor;
	private final int pagesize = 20;
	private  int pageId = 0;
    private LinearLayout footLoadingLayout;
    private ProgressBar footLoadingPB;
    private TextView footLoadingText;
    private Button searchBtn;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_public_groups);

        initView();

        //获取及显示数据
        setListener();
        loadAndShowData();
        registerPublicGroupChangedReceiver();

        
	}


    private void setListener() {
        setItemClickListener();
        setScrollListener();
    }

    private void setScrollListener() {
        listView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    if (listView.getCount() != 0) {
                        int lasPos = view.getLastVisiblePosition();
                        if (hasMoreData && !isLoading && lasPos == listView.getCount() - 1) {
                            pageId++;
                            new DownloadPublicGroupTask(PublicGroupsActivity.this, superWeChatApplication.getInstance().getUserName(),
                                    pageId, pagesize).execute();
                            loadAndShowData();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void setItemClickListener() {
        //设置item点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(PublicGroupsActivity.this, GroupSimpleDetailActivity.class).
                        putExtra("groupinfo", adapter.getItem(position)));
            }
        });
    }

    private void initView() {

        searchBtn = (Button) findViewById(R.id.btn_search);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.list);

        groupsList = new ArrayList<Group>();
        View footView = getLayoutInflater().inflate(R.layout.listview_footer_view, null);
        footLoadingLayout = (LinearLayout) footView.findViewById(R.id.loading_layout);
        footLoadingPB = (ProgressBar)footView.findViewById(R.id.loading_bar);
        footLoadingText = (TextView) footView.findViewById(R.id.loading_text);
        listView.addFooterView(footView, null, false);
        footLoadingLayout.setVisibility(View.GONE);
    }

    /**
	 * 搜索
	 * @param view
	 */
	public void search(View view){
	    startActivity(new Intent(this, PublicGroupsSeachActivity.class));
	}
	
	private void loadAndShowData(){



                try {
                    isLoading = true;
                    final ArrayList<Group> publicGroupLis = superWeChatApplication.getInstance().getPublicGroupList();
                    Log.i("main", "loadAndShowData,publicGroupLis:" + publicGroupLis.size());
        //获取group list


                    for (Group group : publicGroupLis) {
                        if (!groupsList.contains(group)) {
                            groupsList.add(group);
                            Log.i("main", group.toString());
                        }
                    }
                    runOnUiThread(new Runnable() {

                        public void run() {
                            searchBtn.setVisibility(View.VISIBLE);
//                            groupsList.addAll(returnGroups);
                            if(publicGroupLis.size() != 0){
                                //获取cursor
//                                cursor = result.getCursor();
                                if(publicGroupLis.size() < publicGroupLis.size())
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                            }
                            if(isFirstLoading){
                                pb.setVisibility(View.INVISIBLE);
                                isFirstLoading = false;
                                //设置adapter
                                adapter = new GroupsAdapter(PublicGroupsActivity.this, 1, groupsList);
                                listView.setAdapter(adapter);
                            }else{
                                if(publicGroupLis.size() < pagesize * (pageId + 1)){
                                    hasMoreData = false;
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                                    footLoadingPB.setVisibility(View.GONE);
                                    footLoadingText.setText("No more data");
                                }
                                adapter.notifyDataSetChanged();
                            }
                            isLoading = false;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            isLoading = false;
                            pb.setVisibility(View.INVISIBLE);
                            footLoadingLayout.setVisibility(View.GONE);
                            Toast.makeText(PublicGroupsActivity.this, "加载数据失败，请检查网络或稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
//        }).start();
	}
	/**
	 * adapter
	 *
	 */
	private class GroupsAdapter extends BaseAdapter {

		private LayoutInflater inflater;
        ArrayList<Group> mGroupList;

		public GroupsAdapter(Context context, int res, ArrayList<Group> groups) {
			this.inflater = LayoutInflater.from(context);
            this.mGroupList = groups;
        }

        @Override
        public int getCount() {
            return mGroupList == null ? 0 : mGroupList.size();
        }

        @Override
        public Group getItem(int position) {
            return mGroupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_group, null);
			}

			((TextView) convertView.findViewById(R.id.name)).setText(mGroupList.get(position).getMGroupName());
            UserUtils.setPublicGroupBeanAvatar(mGroupList.get(position).getMGroupHxid(),(NetworkImageView)convertView.findViewById(R.id.avatar));
			return convertView;
		}
	}
	
	public void back(View view){
		finish();
	}

    class PublicGroupChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            loadAndShowData();
        }
    }

    PublicGroupChangedReceiver mPublicGroupChangedReceiver;

    private void registerPublicGroupChangedReceiver() {
        mPublicGroupChangedReceiver = new PublicGroupChangedReceiver();
        IntentFilter filter = new IntentFilter("update_public_group");
        registerReceiver(mPublicGroupChangedReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPublicGroupChangedReceiver != null) {
            unregisterReceiver(mPublicGroupChangedReceiver);
        }
    }
}

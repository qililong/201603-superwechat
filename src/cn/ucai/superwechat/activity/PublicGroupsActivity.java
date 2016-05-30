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
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.util.EMLog;

import java.util.ArrayList;
import java.util.List;

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
	}


    private void setListener() {
        setItemClickListener();
        setScrollListener();
        registerPublicGroupChangedReceiver();
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
                            Log.i("main", pageId + "");
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
                    for (Group group : publicGroupLis) {
                        if (!groupsList.contains(group)) {
                            groupsList.add(group);
                            Log.i("main", "groupsList:" + groupsList.size());
                        }
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            searchBtn.setVisibility(View.VISIBLE);
//                            groupsList.addAll(returnGroups);
                            if(publicGroupLis.size() != 0) {
                                //获取cursor
//                                cursor = result.getCursor();
                                if (groupsList.size() < publicGroupLis.size())
                                    Log.i("main", "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                            }
                            if(isFirstLoading){
                                pb.setVisibility(View.INVISIBLE);
                                isFirstLoading = false;
                                //设置adapter
                                adapter = new GroupsAdapter(PublicGroupsActivity.this, 1, groupsList);
                                listView.setAdapter(adapter);
                            }else{
                                if(groupsList.size() < pagesize * (pageId + 1)){
                                    Log.i("main", "LLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
                                    hasMoreData = false;
                                    footLoadingLayout.setVisibility(View.VISIBLE);
                                    footLoadingPB.setVisibility(View.GONE);
                                    footLoadingText.setText("No more data");
                                }
                                Log.i("main", "groupsListSize:" + groupsList.size());
                                adapter.notifyDataSetChanged();
                                Log.i("main", "OVER");
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
	private class GroupsAdapter extends BaseAdapter implements SectionIndexer {

        private   final  String TAG = GroupsAdapter.class.getName();

		private LayoutInflater inflater;
        ArrayList<Group> mGroupList;

        ArrayList<Group> copyGroupList;
        ArrayList<String> list;
        private MyFilter myFilter;
        Context context;

        private SparseIntArray positionOfSection;
        private SparseIntArray sectionOfPosition;
        private boolean notiyfyByFilter;

		public GroupsAdapter(Context context, int res, ArrayList<Group> groups) {
			this.inflater = LayoutInflater.from(context);
            this.mGroupList = groups;
            this.context = context;
            copyGroupList = new ArrayList<>();
            this.copyGroupList = groups;
            Log.i("main", "groups:" + groups.size());
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
            UserUtils.setPublicGroupBeanAvatar(mGroupList.get(position).getMGroupHxid(),
                    (NetworkImageView) convertView.findViewById(R.id.avatar));
            Log.i("main", "YYYYYYYYYYYYYYYYYYYYYYYYY");
            return convertView;
		}
        @Override
        public Object[] getSections() {
            positionOfSection = new SparseIntArray();
            sectionOfPosition = new SparseIntArray();
            int count = getCount();
            list = new ArrayList<String>();
            list.add(context.getString(cn.ucai.superwechat.R.string.search_header));
            positionOfSection.put(0, 0);
            sectionOfPosition.put(0, 0);
            for (int i = 1; i < count; i++) {

                String letter = getItem(i).getHeader();
                Log.e(TAG, "contactadapter getsection getHeader:" + letter + " name:" + getItem(i).getMGroupName());
                int section = list.size() - 1;
                if (list.get(section) != null && !list.get(section).equals(letter)) {
                    list.add(letter);
                    section++;
                    positionOfSection.put(section, i);
                }
                sectionOfPosition.put(i, section);
            }
            return list.toArray(new String[list.size()]);
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return positionOfSection.get(sectionIndex);
        }

        @Override
        public int getSectionForPosition(int position) {
            return sectionOfPosition.get(position);
        }

        public Filter getFilter() {
            if(myFilter==null){
                myFilter = new MyFilter(mGroupList);
            }
            return myFilter;
        }

        private class  MyFilter extends Filter{
            List<Group> mOriginalList = null;

            public MyFilter(List<Group> myList) {
                this.mOriginalList = myList;
            }

            @Override
            protected synchronized FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();
                if(mOriginalList==null){
                    mOriginalList = new ArrayList<Group>();
                }
                EMLog.d(TAG, "contacts original size: " + mOriginalList.size());
                EMLog.d(TAG, "contacts copy size: " + copyGroupList.size());

                if(prefix==null || prefix.length()==0){
                    results.values = copyGroupList;
                    results.count = copyGroupList.size();
                }else{
                    String prefixString = prefix.toString();
                    final int count = mOriginalList.size();
                    final ArrayList<Group> newValues = new ArrayList<Group>();
                    for(int i=0;i<count;i++){
                        final Group user = mOriginalList.get(i);
                        String username = user.getMGroupName();

                        if(username.contains(prefixString) || user.getMGroupHxid().contains(prefixString)){
                            newValues.add(user);
                        }
                        else{
                            final String[] words = username.split(" ");
                            final int wordCount = words.length;

                            // Start at index 0, in case valueText starts with space(s)
                            for (int k = 0; k < wordCount; k++) {
                                if (words[k].contains(prefixString)) {
                                    newValues.add(user);
                                    break;
                                }
                            }
                        }
                    }
                    results.values=newValues;
                    results.count=newValues.size();
                }
                EMLog.d(TAG, "contacts filter results size: " + results.count);
                return results;
            }

            @Override
            protected synchronized void publishResults(CharSequence constraint,
                                                       FilterResults results) {
                mGroupList.clear();
                mGroupList.addAll((List<Group>)results.values);
                EMLog.d(TAG, "publish contacts filter results size: " + results.count);
                if (results.count > 0) {
                    notiyfyByFilter = true;
                    notifyDataSetChanged();
                    notiyfyByFilter = false;
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }


        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if(!notiyfyByFilter){
                copyGroupList.clear();
                copyGroupList.addAll(mGroupList);
            }
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

package cn.ucai.fulicenter.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FulicenterActivity;
import cn.ucai.fulicenter.adapter.CategoryAdapter;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment {
    FulicenterActivity mContext;
    ArrayList<CategoryGroupBean> mGroupList;
    ArrayList<ArrayList<CategoryChildBean>> mChildList;
    ExpandableListView melvCategory;

    CategoryAdapter mAdapter;

    int groupCount;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = (FulicenterActivity) getActivity();
        View layout = inflater.inflate(R.layout.fragment_category, container, false);
        initView(layout);
        initData();
        return layout;
    }

    private void initData() {
        mGroupList = new ArrayList<CategoryGroupBean>();
        mChildList = new ArrayList<ArrayList<CategoryChildBean>>();
        try {
            String path = new ApiParams().getRequestUrl(I.REQUEST_FIND_CATEGORY_GROUP);
            mContext.executeRequest(new GsonRequest<CategoryGroupBean[]>(path, CategoryGroupBean[].class,
                    responseDownCategoryListListener(), mContext.errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<CategoryGroupBean[]> responseDownCategoryListListener() {
        return new Response.Listener<CategoryGroupBean[]>() {
            @Override
            public void onResponse(CategoryGroupBean[] categoryGroupBeans) {
                if (categoryGroupBeans != null) {
                    mGroupList = Utils.array2List(categoryGroupBeans);
                    int i = 0;
                    for (CategoryGroupBean groupBean : mGroupList) {
                        try {
                            mChildList.add(i, new ArrayList<CategoryChildBean>());
                            String path = new ApiParams().with(I.CategoryChild.PARENT_ID, groupBean.getId() + "")
                                    .with(I.PAGE_ID, "0").with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT + "")
                                    .getRequestUrl(I.REQUEST_FIND_CATEGORY_CHILDREN);
                            mContext.executeRequest(new GsonRequest<CategoryChildBean[]>(path, CategoryChildBean[].class,
                                    responseDownCategoryChildListListener(i), mContext.errorListener()));
                            i++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    private Response.Listener<CategoryChildBean[]> responseDownCategoryChildListListener(final int i) {
        return new Response.Listener<CategoryChildBean[]>() {
            @Override
            public void onResponse(CategoryChildBean[] categoryChildBeans) {
                groupCount++;
                if (categoryChildBeans != null) {
                    ArrayList<CategoryChildBean> childList = Utils.array2List(categoryChildBeans);
                    if (childList != null) {
                        mChildList.set(i, childList);
                    }
                }
                if (mGroupList.size() == groupCount) {
                    mAdapter.addItems(mGroupList, mChildList);
                }
            }
        };
    }

    private void initView(View view) {
        melvCategory = (ExpandableListView) view.findViewById(R.id.excategory);
        melvCategory.setGroupIndicator(null);
        mGroupList = new ArrayList<CategoryGroupBean>();
        mChildList = new ArrayList<ArrayList<CategoryChildBean>>();
        mAdapter = new CategoryAdapter(mContext, mGroupList, mChildList);
        melvCategory.setAdapter(mAdapter);
    }

}

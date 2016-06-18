package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/18.
 */
public class CategoryAdapter extends BaseExpandableListAdapter{

    Context mContext;
    ArrayList<CategoryGroupBean> mGroupList;
    ArrayList<ArrayList<CategoryChildBean>> mChildList;

    public CategoryAdapter(Context mContext, ArrayList<CategoryGroupBean> mGroupList, ArrayList<ArrayList<CategoryChildBean>> mChildList) {
        this.mContext = mContext;
        this.mGroupList = mGroupList;
        this.mChildList = mChildList;
    }

    @Override
    public int getGroupCount() {
        return mGroupList == null?0:mGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildList==null||mChildList.get(groupPosition)==null?0:mChildList.get(groupPosition).size();
    }

    @Override
    public CategoryGroupBean getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }

    @Override
    public CategoryChildBean getChild(int groupPosition, int childPosition) {
        return mChildList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewGroupHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_category_group, null);
            holder = new ViewGroupHolder();
            holder.ivIndicator = (ImageView) convertView.findViewById(R.id.ivIndicator);
            holder.ivGroupThumb = (NetworkImageView) convertView.findViewById(R.id.ivGroupThumb);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.tvGroupName);
            convertView.setTag(holder);
        } else {
            holder = (ViewGroupHolder) convertView.getTag();
        }
        CategoryGroupBean group = getGroup(groupPosition);
        holder.tvGroupName.setText(group.getName());
        String imageUrl = group.getImageUrl();
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL + imageUrl;
        ImageUtils.setThumb(url, holder.ivGroupThumb);
        if (isExpanded) {
            holder.ivIndicator.setImageResource(R.drawable.expand_off);
        } else {
            holder.ivIndicator.setImageResource(R.drawable.expand_on);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewChildHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_cateogry_child, null);
            holder = new ViewChildHolder();
            holder.layout_category_child = (RelativeLayout) convertView.findViewById(R.id.layout_category_child);
            holder.ivCategoryChildThumb = (NetworkImageView) convertView.findViewById(R.id.ivCategoryChildThumb);
            holder.tvCategoryChildName = (TextView) convertView.findViewById(R.id.tvCategoryChildName);
            convertView.setTag(holder);
        } else {
            holder = (ViewChildHolder) convertView.getTag();
        }
        CategoryChildBean child = getChild(groupPosition, childPosition);
        String name = child.getName();
        holder.tvCategoryChildName.setText(name);

        String imageUrl = child.getImageUrl();
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_UR + imageUrl;
        ImageUtils.setThumb(url, holder.ivCategoryChildThumb);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class ViewGroupHolder {
        NetworkImageView ivGroupThumb;
        TextView tvGroupName;
        ImageView ivIndicator;
    }

    class ViewChildHolder {
        RelativeLayout layout_category_child;
        NetworkImageView ivCategoryChildThumb;
        TextView tvCategoryChildName;
    }

    public void addItems(ArrayList<CategoryGroupBean> groupList, ArrayList<ArrayList<CategoryChildBean>> childList) {
        this.mGroupList.addAll(groupList);
        this.mChildList.addAll(childList);
        notifyDataSetChanged();
    }
}

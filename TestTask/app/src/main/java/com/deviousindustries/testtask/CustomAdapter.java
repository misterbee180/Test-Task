package com.deviousindustries.testtask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deviousindustries.testtask.R;

import java.util.ArrayList;
import java.util.TreeSet;

public class CustomAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_GROUP = 2;
    private static final int TYPE_MAX_COUNT = TYPE_GROUP + 1;

    private ArrayList<itemDetail> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    private TreeSet mSeparatorsSet = new TreeSet();
    private TreeSet mGroupsSet = new TreeSet();

    private class itemDetail{
        private String mName;
        private Long mId;

        private itemDetail(String pName, Long pId){
            mName = pName;
            mId = pId;
        }
    }

    public CustomAdapter(Context pContext) {
        mInflater = (LayoutInflater)pContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void  addItem(final String pName, Long pId) {
        mData.add(new itemDetail(pName, pId));
        notifyDataSetChanged();
    }

    void addSeparatorItem(final String pName) {
        mData.add(new itemDetail(pName, (long)-1));
        // save separator position
        mSeparatorsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    void addGroupItem(final String pName, final Long pSession){
        mData.add(new itemDetail(pName, pSession));
        //save group position
        mGroupsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int result =  TYPE_ITEM;
        result = mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : result;
        result = mGroupsSet.contains(position) ? TYPE_GROUP : result;

        return result;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position){
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_ITEM:
                return true;
            case TYPE_SEPARATOR:
                return false;
            case TYPE_GROUP:
                return true;
        }
        return false;
    }

    public static class ViewHolder {
        TextView textView;
        public TextView id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);
        System.out.println("getView " + position + " " + convertView + " type = " + type);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.task_item1, null);
                    holder.textView = convertView.findViewById(android.R.id.text1);
                    holder.id = convertView.findViewById(R.id.taskId);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.seperator_item1, null);
                    holder.textView = convertView.findViewById(android.R.id.text1);
                    holder.id = new TextView(convertView.getContext());
                    break;
                case TYPE_GROUP:
                    convertView = mInflater.inflate(R.layout.task_group1, null);
                    holder.textView = convertView.findViewById(android.R.id.text1);
                    holder.id = new TextView(convertView.getContext());
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(mData.get(position).mName);
        holder.id.setText(mData.get(position).mId.toString());
        return convertView;
    }
}


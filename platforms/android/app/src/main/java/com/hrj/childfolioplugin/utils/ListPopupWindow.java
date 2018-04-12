package com.hrj.childfolioplugin.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hrj.childfolioplugin.view.TextCircleImageView;
import com.my.testcamera.R;

import java.util.Arrays;
import java.util.List;

public class ListPopupWindow {

    public class HeadItem {
        String headUrl;
        String firstName;
        String lastName;

        public HeadItem(String headUrl, String firstName, String lastName) {
            this.headUrl = headUrl;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    public interface ListPopupWindowListener {
        void onItemClick(int position);
    }

    private ListPopupWindowListener mItemClickListener;
    private PopupWindow mPopupWindow;
    private RecyclerView mContainer;
    private Context mContext;
    private TextView mTitle;

    public ListPopupWindow(Context context) {
        mContext = context;
        mPopupWindow = new PopupWindow(initPopWindowView(context), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
    }

    private View initPopWindowView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_list, null);
        mTitle = (TextView) view.findViewById(R.id.tv_title);
        mContainer = (RecyclerView) view.findViewById(R.id.rv_container);
        mContainer.setLayoutManager(new LinearLayoutManager(context));
        return view;
    }

    public void showPopupWindow(View anchorView, String title, List<String> items, ListPopupWindowListener listener) {
        showPopupWindow(anchorView, title, items, null, listener);
    }

    public void showPopupWindow(View anchorView, String title, String[] items, ListPopupWindowListener listener) {
        showPopupWindow(anchorView, title, Arrays.asList(items), null, listener);
    }

    public void showPopupWindow(View anchorView, String title, List<String> items, List<HeadItem> headers, ListPopupWindowListener listener) {
        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(items, headers);
        mContainer.setAdapter(adapter);
        if (TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.GONE);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(title);
        }
        mItemClickListener = listener;
        mPopupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_popup_window_bg));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        if (AppVersionUtils.isUpLOLLIPOP()) {
            mPopupWindow.setElevation(10);
        }

        Rect location = locateView(anchorView);
        mPopupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.LEFT, location.left, location.bottom);
    }

    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    private class ListPopupWindowAdapter extends RecyclerView.Adapter {
        List<String> items;
        List<HeadItem> headers;

        public ListPopupWindowAdapter(List<String> items, List<HeadItem> headers) {
            this.items = items;
            this.headers = headers;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (null == headers) {//没有头像
                return new PopupWindowTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_popupwindow_item, parent, false));
            } else {
                return new PopupWindowHeadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_popupwindow_img_item, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PopupWindowTextViewHolder) {
                PopupWindowTextViewHolder textViewHolder = (PopupWindowTextViewHolder) holder;
                textViewHolder.bindView(items.get(position), position);
            } else if (holder instanceof PopupWindowHeadViewHolder) {
                PopupWindowHeadViewHolder headViewHolder = (PopupWindowHeadViewHolder) holder;
                headViewHolder.bindView(items.get(position), headers.get(position), position);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class PopupWindowTextViewHolder extends RecyclerView.ViewHolder {
        private TextView mView;

        public PopupWindowTextViewHolder(View itemView) {
            super(itemView);
            mView = (TextView) itemView;
        }

        public void bindView(String item, final int position) {
            mView.setText(item);
            if (item.equals("删除") || item.equals("Delete"))
                mView.setTextColor(Color.RED);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != mItemClickListener) {
                        mItemClickListener.onItemClick(position);
                    }
                    mPopupWindow.dismiss();
                }
            });
        }
    }

    private class PopupWindowHeadViewHolder extends RecyclerView.ViewHolder {
        TextCircleImageView headerView;
        TextView title;
        View mView;

        public PopupWindowHeadViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            title = (TextView) itemView.findViewById(R.id.tv_title);
            headerView = (TextCircleImageView) itemView.findViewById(R.id.icon_header);
        }

        private void bindView(String titleStr, HeadItem headItem, int position) {
            if (!TextUtils.isEmpty(headItem.headUrl)) {
                headerView.setImageUrl(headItem.headUrl);
            } else {
                headerView.setHeadText(headItem.firstName, headItem.lastName);
            }
            title.setText(titleStr);
            if (null != mItemClickListener) {
                mItemClickListener.onItemClick(position);
            }
        }
    }
}

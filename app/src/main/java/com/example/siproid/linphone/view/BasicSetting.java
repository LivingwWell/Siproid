package com.example.siproid.linphone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.siproid.R;

public class BasicSetting extends LinearLayout {
    final Context mContext;
    View mView;
    private TextView mTitle;
    private TextView mSubtitle;
    SettingListener mListener;

    public BasicSetting(Context context) {
        super(context);
        mContext = context;
        init(null, 0, 0);
    }

    public BasicSetting(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0, 0);
    }

    public BasicSetting(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(attrs, defStyleAttr, 0);
    }

    BasicSetting(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init(attrs, defStyleAttr, defStyleRes);
    }

    void inflateView() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.settings_widget_basic, this, true);
    }

    public void setListener(SettingListener listener) {
        mListener = listener;
    }

    void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflateView();

        mTitle = mView.findViewById(R.id.setting_title);
        mSubtitle = mView.findViewById(R.id.setting_subtitle);

        RelativeLayout rlayout = mView.findViewById(R.id.setting_layout);
        rlayout.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTitle.isEnabled() && mListener != null) {
                            mListener.onClicked();
                        }
                    }
                });

        if (attrs != null) {
            TypedArray a =
                    mContext.getTheme()
                            .obtainStyledAttributes(
                                    attrs, R.styleable.Settings, defStyleAttr, defStyleRes);
            try {
                String title = a.getString(R.styleable.Settings_title);
                if (title != null) {
                    mTitle.setText(title);
                } else {
                    mTitle.setVisibility(GONE);
                }

                String subtitle = a.getString(R.styleable.Settings_subtitle);
                if (subtitle != null) {
                    mSubtitle.setText(subtitle);
                } else {
                    mSubtitle.setVisibility(GONE);
                }
            } finally {
                a.recycle();
            }
        }
    }

    public void setTitle(String title) {
        mTitle.setText(title);
        mTitle.setVisibility(title == null || title.isEmpty() ? GONE : VISIBLE);
    }

    public void setSubtitle(String subtitle) {
        mSubtitle.setText(subtitle);
        mSubtitle.setVisibility(subtitle == null || subtitle.isEmpty() ? GONE : VISIBLE);
    }

    public void setEnabled(boolean enabled) {
        mTitle.setEnabled(enabled);
        mSubtitle.setEnabled(enabled);
    }
}

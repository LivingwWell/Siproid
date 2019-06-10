package com.example.siproid.linphone.view;

public interface SettingListener {
    void onClicked();

    void onTextValueChanged(String newValue);

    void onBoolValueChanged(boolean newValue);

    void onListValueChanged(int position, String newLabel, String newValue);
}

package com.example.siproid.pjsip;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;

import java.util.logging.Handler;

public class MyAccount extends Account {
    Handler handler;
    /***
     *  当注册或注销已经启动时通知申请。
     *  请注意，这只会通知初始注册和注销。一旦注册会话处于活动状态，后续刷新将不会导致此回调被调用。
     * @param prm
     */
    @Override
    public void onRegState(OnRegStateParam prm) {
    }


    /***
     *  来电话啦
     */
    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
    }


}

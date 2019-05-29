package com.example.siproid.linphone;

import org.linphone.core.Call;
import org.linphone.core.RegistrationState;

public abstract class PhoneServiceCallback {
    /**
     * 注册状态
     * @param registrationState
     */
    public void registrationState(RegistrationState registrationState) {}
    /**
     * 注册状态
     * @param registrationState
     */
    public void unRegistrationState(RegistrationState registrationState) {}

    /**
     * 来电状态
     * @param linphoneCall
     */
    public void incomingCall(Call linphoneCall) {}

    /**
     * 电话接通
     */
    public void callConnected() {}

    /**
     * 电话被挂断
     */
    public void callReleased() {}
    /**
     * 电话接通
     */
    public void callStart() {}

}

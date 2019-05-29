package com.example.siproid.pjsip;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.siproid.R;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.CallSetting;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;

public class PjsipActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

    private Button button, button2;
    private TextView textView;
    private String sipAddress = "202.104.64.74";
    private MyAccount myAccount;
    private MyCall myCall;
    private static CallInfo lastCallInfo;
    private final Handler handler = new Handler(this);
    private static Handler handler_;

    static {
        System.loadLibrary("pjsua2");
        System.out.println("Library loaded");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        textView=findViewById(R.id.sample_text);
        button.setOnClickListener(this);
        regiser(this, "1996", "123456", sipAddress);

        handler_ = handler;
        if (myCall != null) {
            try {
                lastCallInfo = myCall.getInfo();
                updataeCallState(lastCallInfo);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
//        } else {
//                 updataeCallState(lastCallInfo);
//        }
    }

    private void regiser(Context context, final String account, final String pwd, final String ip) {
        try {
            AccountConfig acfg = new AccountConfig();
            acfg.getNatConfig().setIceEnabled(true);
            acfg.getVideoConfig().setAutoTransmitOutgoing(true);
            acfg.getVideoConfig().setAutoShowIncoming(true);
            acfg.setIdUri("sip:" + account + "@" + ip);
            acfg.getRegConfig().setRegistrarUri("sip:" + ip);

            AuthCredInfo cred = new AuthCredInfo("digest", "*", account, 0, pwd);
            acfg.getSipConfig().getAuthCreds().add(cred);
            //创建帐户
            myAccount = new MyAccount();
            myAccount.create(acfg);
            Log.d("PjsipActivity ", "注册成功");
            System.out.println(myAccount.getInfo().getUri());
        } catch (Exception e) {
            Log.e("PjsipActivity ", "注册失败" + e.getMessage());
        }
    }


    //打电话
    private void CallPhone(String number, String ip) {
        myCall = new MyCall(myAccount, -1);
        CallOpParam prm = new CallOpParam();
        CallSetting opt = prm.getOpt();
        opt.setAudioCount(1);
        opt.setVideoCount(1);
        String dst_uri = "sip:" + number + "@" + ip;
        try {
            myCall.makeCall(dst_uri, prm);
        } catch (Exception e) {
            myCall.delete();
            Log.e("PjsipActivity", "error" + e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                CallPhone("1997", "202.104.64.74");
                break;
            case R.id.button2:
                break;
        }
    }

    public void updataeCallState(CallInfo ci) {

        String call_state = "";
        if (lastCallInfo.getRole() == pjsip_role_e.PJSIP_ROLE_UAC) {

        }

        if (ci.getState().swigValue() < pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
            if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAS) {
                call_state = "Incoming call..";
            } else {
                call_state = ci.getStateText();
            }
        } else if (ci.getState().swigValue() >= pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
            call_state = ci.getStateText();
            if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {

            } else if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                call_state = "Call diconnected" + ci.getLastReason();
            }
        }
        textView.setText(call_state);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 1) {

        }
        return false;
    }
}

package com.example.siproid.sip;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.siproid.R;

public class WalkieTalkieActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "WalkieTalkieActivity";
    private Button button1, button2;
    public SipManager manager = null;
    public SipAudioCall call = null;
    public SipProfile mSipProfile = null;
    public IncomingCallReceiver callReceiver;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        initVIew();
//        initSip();
        registerInComingCallReceiver();

    }

    private void initVIew() {
        button1 = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
    }

    private void registerInComingCallReceiver() {
        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
    }

    private void initSip() {
        try {
            //初始化
            manager = SipManager.newInstance(this);
            SipProfile.Builder builder = new SipProfile.Builder("1996", "202.104.64.74");
            builder.setPassword("123456");
            mSipProfile = builder.build();
            //设置接听电话广播
            Intent intent = new Intent();
            intent.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = (PendingIntent) PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
            manager.open(mSipProfile, pi, null);
            //登录状态监听
            manager.setRegistrationListener(mSipProfile.getUriString(), new SipRegistrationListener() {
                @Override
                public void onRegistering(String localProfileUri) {//正在注册
                    Log.d(TAG, "正在注册: " + localProfileUri);
                }

                @Override
                public void onRegistrationDone(String localProfileUri, long expiryTime) {//注册成功
                    Log.d(TAG, "注册成功: " + localProfileUri + "expiryTime:" + expiryTime + mSipProfile.getProtocol());
                }

                @Override
                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {//注册失败
                    Log.d(TAG, "注册失败: " + localProfileUri + "errorCode:" + errorCode + "errorMessage:" + errorMessage);
                }
            });
        } catch (Exception e) {

        }
    }

    private void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (mSipProfile != null) {
                manager.close(mSipProfile.getUriString());
            }
        } catch (Exception e) {
            Log.d(TAG, "closeLocalProfile: " + e);
        }
    }

    public void initiateCall(String adresss) {
        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onReadyToCall(SipAudioCall call) {
                    super.onReadyToCall(call);
                    Log.d(TAG, "onReadyToCall: ");
                }

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    super.onRinging(call, caller);
                    Log.d(TAG, "onRinging: ");
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    Log.d(TAG, "onCallEstablished: ");
                    call.startAudio();
                    call.setSpeakerMode(true);
                    call.toggleMute();
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    Log.d(TAG, "onCallEnded: ");

                }
            };
            call = manager.makeAudioCall(mSipProfile.getUriString(), adresss, listener, 30);
            call.toggleMute();
        } catch (Exception e) {
            if (mSipProfile != null) {
                try {
                    manager.close(mSipProfile.getUriString());
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

    private Handler handler = new Handler();

    public void initGetSipSession() {
        //SipSession sipSession=manager.getSessionFor();
        SipSession.Listener listener = new SipSession.Listener() {
            @Override
            public void onCallEstablished(SipSession session, String sessionDescription) {
                super.onCallEstablished(session, sessionDescription);
                Log.d(TAG, "onCallEstablished: " + sessionDescription);
            }
        };
    }


    public native String stringFromJNI();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                initiateCall("sip:1997@202.104.64.74");
                break;
            case R.id.button2:
                try {
                    call.endCall();
                } catch (SipException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

package com.example.siproid.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.util.Log;

import com.example.siproid.sip.WalkieTalkieActivity;

public class IncomingCallReceiver extends BroadcastReceiver {
    private static String TAG="IncomingCallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        SipAudioCall incomingCall=null;
        try{
            SipAudioCall.Listener listener=new SipAudioCall.Listener(){
                @Override

                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        Log.d(TAG, "onRinging: ");
                        call.answerCall(30);
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            WalkieTalkieActivity walkieTalkieActivity = (WalkieTalkieActivity) context;
            incomingCall=walkieTalkieActivity.manager.takeAudioCall(intent,listener);
            incomingCall.answerCall(30);
            incomingCall.startAudio();
            incomingCall.setSpeakerMode(true);
            if (incomingCall.isMuted()){
                incomingCall.toggleMute();
            }
            walkieTalkieActivity.call=incomingCall;

        }catch (Exception e){
              if (incomingCall!=null){
                  incomingCall.close();
              }
        }
    }
}

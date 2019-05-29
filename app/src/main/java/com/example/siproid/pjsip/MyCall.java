package com.example.siproid.pjsip;

import com.example.siproid.sip.OnCallStateListener;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;
import org.pjsip.pjsua2.pjsua_call_media_status;

public class MyCall extends Call {
    public OnCallStateListener onCallStateListener;

    public MyCall(MyAccount cPtr, int cMemoryOwn) {
        super(cPtr, cMemoryOwn);
    }

    /***
     * 当通话状态改变时通知应用程序。
     * 然后，应用程序可以通过调用getInfo（）函数来查询调用信息以获取详细调用状态。
     * @param prm
     */
    @Override
    public void onCallState(OnCallStateParam prm) {
        super.onCallState(prm);
        try {
            pjsip_inv_state state = getInfo().getState();
            pjsip_role_e role = getInfo().getRole();
            //电话呼出
            if (role == pjsip_role_e.PJSIP_ROLE_UAC) {
                //电话呼入
            } else if (role == pjsip_role_e.PJSIP_ROLE_UAS) {

            }

            if (state == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
                onCallStateListener.calling();
            } else if (state == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
                onCallStateListener.early();
            } else if (state == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
                onCallStateListener.conmecting();
            } else if (state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                onCallStateListener.confirmed();
            } else if (state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                onCallStateListener.disconnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /***
     * 通话中媒体状态发生变化时通知应用程序。
     * 正常的应用程序需要实现这个回调，例如将呼叫的媒体连接到声音设备。当使用ICE时，该回调也将被调用以报告ICE协商失败。
     * @param prm
     */
    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        CallInfo ci;
        try {
            ci = getInfo();
        } catch (Exception e) {
            return;
        }
        CallMediaInfoVector cmiv = ci.getMedia();
        for (int i = 0; i < cmiv.size(); i++) {
            CallMediaInfo cmi = cmiv.get(i);
            if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                    (cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                            cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)
            ) {
                Media m = getMedia(i);
                AudioMedia am = AudioMedia.typecastFromMedia(m);
                try {
                    PJSipUtil.ep.audDevManager().getCaptureDevMedia().startTransmit(am);
                    am.startTransmit(PJSipUtil.ep.audDevManager().getPlaybackDevMedia());
                } catch (Exception e) {
                    continue;
                }

            }
        }
    }
}

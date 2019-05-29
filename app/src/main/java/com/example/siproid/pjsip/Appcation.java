package com.example.siproid.pjsip;

import android.app.Application;
import android.util.Log;

import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pjsip_transport_type_e;


public class Appcation extends Application {
    static {
        System.loadLibrary("pjsua2");
        System.out.println("pjsip============================> Library loaded");
    }
    public  Endpoint ep;
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    //初始化
    private void init() {
        try {
            if (ep == null) {
                ep = new Endpoint();
                Log.d("PjsipActivity", "初始化成功" );
            }
            //创建端点
            ep.libCreate();
            //初始化端点
            EpConfig epConfig = new EpConfig();
            ep.libInit(epConfig);
            //创建SIP传输.显示错误处理示例
            TransportConfig sipTpConfig = new TransportConfig();
            sipTpConfig.setPort(5060);
            ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, sipTpConfig);
            //启动库
            ep.libStart();
        } catch (Exception e) {
            Log.e("PjsipActivity", "初始化失败" + e.getMessage());
        }
    }
}

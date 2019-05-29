package com.example.siproid.linphone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.siproid.R;

import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

public class LinphoneActivity extends AppCompatActivity implements View.OnClickListener {
    private Core core;
    private String sipAddress = "sip:1996@202.104.64.74",
            passsword = "123456",
            username = "1996",
            domain = "202.104.64.74",
            port = "5060";
    private Button button, button2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button.setOnClickListener(this);
        register();
    }

    private void register() {
        Factory.instance().setDebugMode(true, "Linphone");
        core = Factory.instance().createCore(null, null, this);
        core.start();
        Address address = Factory.instance().createAddress(sipAddress);
        //配置权限认证信息
        AuthInfo authInfo=Factory.instance().createAuthInfo(username,null,passsword,null,domain,domain);
        core.clearAllAuthInfo();
        core.addAuthInfo(authInfo);
        //配置服务器地址
        ProxyConfig proxyConfig=core.createProxyConfig();
        proxyConfig.enablePublish(true);
        proxyConfig.setExpires(2000);
        core.addProxyConfig(proxyConfig);
        core.setDefaultProxyConfig(proxyConfig);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:

                break;
            case R.id.button2:
                break;
        }
    }
}

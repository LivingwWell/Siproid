package com.example.siproid.linphone;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.siproid.R;
import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import org.linphone.core.Call;
import org.linphone.core.Core;
import org.linphone.core.PayloadType;
import org.linphone.core.TransportType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LinphoneActivity extends AppCompatActivity {
    public static final String RECEIVE_MAIN_ACTIVITY = "receive_main_activity";
    @BindView(R.id.sample_text)
    TextView sampleText;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.id_call_stateinfo)
    TextView idCallStateinfo;
    @BindView(R.id.videocall)
    Button videocall;
    @BindView(R.id.button4)
    Button button4;

    private String sipAddress = "sip:202.104.64.74",
            identify = "sip:1996@202.104.64.74",
            passsword = "123",
            username = "1996",
            domain = "202.104.64.74",
            port = "5060";
    private MainActivityReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        isPermissions();

        //注册广播
        IntentFilter intentFilter = new IntentFilter(RECEIVE_MAIN_ACTIVITY);
        mReceiver = new MainActivityReceiver();
        registerReceiver(mReceiver, intentFilter);

    }

    private void getVideoCodec() {
        LinphoneMiniManager miniManager = LinphoneMiniManager.getInstance();
        miniManager.getVideoCodec();
    }

    //权限
    private void isPermissions() {
        if (PermissionsUtil.hasPermission(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})) {
            //有
            Intent intentOne = new Intent(this, LinphoneMiniManager.class);
            startService(intentOne);
        } else {
            PermissionsUtil.requestPermission(this, new PermissionListener() {

                public void permissionGranted(@NonNull String[] permissions) {
                    //用户授予了
                    Intent intentOne = new Intent(LinphoneActivity.this, LinphoneMiniManager.class);
                    startService(intentOne);
                }

                public void permissionDenied(@NonNull String[] permissions) {
                    //用户拒绝了访问摄像头的申请
                    Toast.makeText(LinphoneActivity.this, "您没有授权将无法启用网络电话!", Toast.LENGTH_LONG).show();
                }
            }, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA});
        }
    }


    //注册
    private void register() {
        if (!LinphoneMiniManager.isReady()) {
            Toast.makeText(LinphoneActivity.this, "Service没准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        LinphoneMiniManager instance = LinphoneMiniManager.getInstance();
        try {
            instance.login("202.104.64.74", username, passsword, "5060", TransportType.Udp);
            System.out.println("Linphone注册成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getVideoCodec();
    }

    public void boda() {
        if (!LinphoneMiniManager.isReady()) {
            Toast.makeText(LinphoneActivity.this, "Service没准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        LinphoneMiniManager instance = LinphoneMiniManager.getInstance();
        instance.call("1998", "202.104.64.74:5060", false);
    }

    //挂断电话
    public void guaduan() {
        if (!LinphoneMiniManager.isReady()) {
            Toast.makeText(LinphoneActivity.this, "Service没准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        LinphoneMiniManager instance = LinphoneMiniManager.getInstance();
        instance.hangUp();
    }

    //接听电话
    public void jie() {
        if (!LinphoneMiniManager.isReady()) {
            Toast.makeText(LinphoneActivity.this, "Service没准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        LinphoneMiniManager instance = LinphoneMiniManager.getInstance();
        instance.jie();
        Call call = instance.getLc().getCurrentCall();
            startActivity(new Intent(LinphoneActivity.this, VideoActivity.class));
                if (call != null && call.getRemoteParams().videoEnabled()) {
        }
    }

    public void videoda() {
        // Toast.makeText(MainActivity.this,"无视频",Toast.LENGTH_SHORT    ).show();

        if (!LinphoneMiniManager.isReady()) {
            Toast.makeText(LinphoneActivity.this, "Service没准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        LinphoneMiniManager instance = LinphoneMiniManager.getInstance();
        instance.call("1998", "202.104.64.74:5060", true);

        startActivity(new Intent(LinphoneActivity.this, VideoActivity.class));

    }

    @OnClick({R.id.button, R.id.button2, R.id.button3, R.id.videocall, R.id.button4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                boda();
                break;
            case R.id.button2:
                guaduan();
                break;
            case R.id.button3:
                register();
                break;
            case R.id.videocall:
                videoda();
                break;
            case R.id.button4:
                jie();
                break;
        }
    }

    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch (action) {
                case "reg_state":
                    sampleText.setText(intent.getStringExtra("data"));
                    break;
                case "show_callinfo":
                    idCallStateinfo.setText(intent.getStringExtra("data"));
                    break;
            }
        }
    }
}

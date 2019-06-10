package com.example.siproid.linphone;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.siproid.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoActivity extends AppCompatActivity {

    @BindView(R.id.id_video_rendering)
    TextureView idVideoRendering;
    @BindView(R.id.id_video_preview)
    TextureView idVideoPreview;
    @BindView(R.id.id_video_gua)
    Button idVideoGua;
    @BindView(R.id.id_video_mute)
    Button idVideoMute;
    @BindView(R.id.id_video_speaker)
    Button idVideoSpeaker;
    @BindView(R.id.id_video_qiev)
    Button idVideoQiev;
    public LinphoneMiniManager instance;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

         instance=LinphoneMiniManager.getInstance();
         LinphoneMiniManager.getInstance().getLc().setNativeVideoWindowId(idVideoRendering);
        LinphoneMiniManager.getInstance().getLc().setNativePreviewWindowId(idVideoPreview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        idVideoPreview = null;
        idVideoRendering = null;
    }



    @OnClick({R.id.id_video_gua, R.id.id_video_mute, R.id.id_video_speaker, R.id.id_video_qiev})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_video_gua:
                instance.hangUp();
                finish();
                break;
            case R.id.id_video_mute:
                break;
            case R.id.id_video_speaker:
                break;
            case R.id.id_video_qiev:
                instance.switch_camera();
                break;
        }
    }
}

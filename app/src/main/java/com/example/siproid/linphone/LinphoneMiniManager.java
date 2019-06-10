package com.example.siproid.linphone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.example.siproid.R;

import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallParams;
import org.linphone.core.CallStats;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListener;
import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.Event;
import org.linphone.core.Factory;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.NatPolicy;
import org.linphone.core.PayloadType;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.TransportType;
import org.linphone.core.Transports;
import org.linphone.core.VersionUpdateCheckResult;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneMiniManager extends Service implements CoreListener {


    private static LinphoneMiniManager mlnstace;
    private Context mContext;
    private Core mLinphoneCore;
    private Timer mTimer;
    private Factory lcFactory;
    private AudioManager mAudioManager;

    public static boolean isReady() {
        return mlnstace != null;
    }

    public static LinphoneMiniManager getInstance() {
        return mlnstace;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        lcFactory = Factory.instance();
        lcFactory.setDebugMode(true, "lilinaini 34>>");
        try {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            copyAssetsFromPackage(basePath);

            mLinphoneCore = lcFactory.createCore(basePath + "/linphonerc", basePath + "linphonerc", mContext);
            mLinphoneCore.addListener(this);

            startlterate();

            setUserAgent();

            mlnstace = this;
            mLinphoneCore.setNetworkReachable(true);//当媒体（RTP）网络可达时，应用程序调用此方法通知linphone核心库。
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            String[] dnsServer = new String[]{"8.8.8.8"};
            mLinphoneCore.setDnsServers(dnsServer);
            mLinphoneCore.enableAdaptiveRateControl(false);
            mLinphoneCore.start();
            setSipPort(-1);

            Intent intent = new Intent(LinphoneActivity.RECEIVE_MAIN_ACTIVITY);
            intent.putExtra("action", "show_version");
            intent.putExtra("data", mLinphoneCore.getVersion());
            sendBroadcast(intent);

            Log.e("ddd", ">>>>>>>>>>>>1116");
        } catch (IOException e) {
            Log.e("创建", "错误" + e.getMessage());
        }
    }

    //获取可用编码列表
    public void getVideoCodec(){
        if (getLc()!=null){
            for ( PayloadType pt:getLc().getVideoPayloadTypes()){
                if (pt.enabled()){
                    System.out.println("支持的编码格式有:");
                    System.out.println(pt.getMimeType()+"\n");
                }
            }
        }
    }

    /**
     * 设置用于每个传输（UDP或TCP）的端口给定传输的零值端口意味着不使用传输。
     * <p>
     * 值LC_SIP_TRANSPORT_RANDOM（-1）表示系统将随机选择端口。
     *
     * @param port
     */
    private void setSipPort(int port) {
        Transports transports = getLc().getTransports();
        transports.setUdpPort(port);//设置Udp端口
        transports.setTcpPort(port);//设置tcp端口
        transports.setTlsPort(-1);//设置tls端口
        getLc().setTransports(transports);
    }

    //配置文件
    private void copyAssetsFromPackage(String basePath) throws IOException {
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
        LinphoneMiniUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        LinphoneMiniUtils.copyFromPackage(mContext, R.raw.assistant_create, new File(basePath + "/assistant_create.rc").getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    /**
     * 设置SIP消息中使用的用户代理字符串。
     * 将SIP消息中使用的用户代理字符串设置为“[ua_name] / [version]”。如果null给出“版本”，则不会打印斜杠字符。如果null 同时给出“ua_name”和“version”，则User-agent标头将为空。
     * 理想情况下，应该在linphone_factory_create_core之后调用此函数。
     */
    private void setUserAgent() {
        try {
            String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("LinphoneMiniAndroid", versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public Core getLc() {
        return mLinphoneCore;
    }

    private void startlterate() {
        TimerTask ITask = new TimerTask() {
            @Override
            public void run() {
                if (mLinphoneCore != null)
                    //主循环
                    mLinphoneCore.iterate();
            }
        };
        mTimer = new Timer("LinphoneMini scheduler");
        //安排指定的任务在指定的延迟后开始进行重复的固定速率执行．
        mTimer.schedule(ITask, 0, 20);
    }


    //用于通知转移进度的回调。
    @Override
    public void onTransferStateChanged(Core core, Call call, Call.State state) {

    }

    //当朋友列表添加到核心朋友列表时报告的回调原型。
    @Override
    public void onFriendListCreated(Core core, FriendList friendList) {

    }

    //用于通知应用程序订阅状态的更改，包括新订阅的到达。
    @Override
    public void onSubscriptionStateChanged(Core core, Event event, SubscriptionState subscriptionState) {

    }

    //已添加回调以通知新的呼叫日志条目,这通常在呼叫终止时完成。
    @Override
    public void onCallLogUpdated(Core core, CallLog callLog) {

    }

    /**
     * 呼叫状态通知回调。
     *
     * @param core
     * @param call  -状态发生变化的调用对象
     * @param state -呼叫的新状态
     * @param s     - 关于状态的消息
     */
    @Override
    public void onCallStateChanged(Core core, Call call, Call.State state, String s) {
        Intent intent = new Intent(LinphoneActivity.RECEIVE_MAIN_ACTIVITY);
        intent.putExtra("action", "show_callinfo");
        intent.putExtra("data", s);
        sendBroadcast(intent);
        Log.e(">onCallStateChanged", call.getRemoteAddress().getUsername() + ">" + call.getRemoteAddressAsString() + ">" + s);
    }

    /**
     * 用于向应用程序或用户请求身份验证信息的回调。
     *
     * @param core
     * @param authInfo-尽可能预先填充用户名，领域和域值的LinphoneAuthInfo
     * @param authMethod                                 -请求的身份验证类型,使用linphone_core_add_auth_info回复此回调
     */
    @Override
    public void onAuthenticationRequested(Core core, AuthInfo authInfo, AuthMethod authMethod) {

    }

    /**
     * 报告特定URI或朋友电话号码的状态模型更改
     *
     * @param core
     * @param friend
     * @param s
     * @param presenceModel
     */
    @Override
    public void onNotifyPresenceReceivedForUriOrTel(Core core, Friend friend, String s, PresenceModel presenceModel) {

    }

    //回调原型告诉LinphoneChatRoom状态已更改。
    @Override
    public void onChatRoomStateChanged(Core core, ChatRoom chatRoom, ChatRoom.State state) {

    }

    @Override
    public void onBuddyInfoUpdated(Core core, Friend friend) {

    }

    //报告网络更改的回调原型由linphone_core_set_network_reachable自动检测或通知。
    @Override
    public void onNetworkReachable(Core core, boolean b) {

    }

    //回调原型，用于通知应用程序有关从网络收到的通知。
    @Override
    public void onNotifyReceived(Core core, Event event, String s, Content content) {

    }

    //报告已收到新订阅请求并等待决策,通过更改此好友的策略来通知此订阅请求的状态
    @Override
    public void onNewSubscriptionRequested(Core core, Friend friend, String s) {

    }

    //回调接收呼叫的质量统计信息。
    @Override
    public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState registrationState, String s) {
        Intent intent = new Intent(LinphoneActivity.RECEIVE_MAIN_ACTIVITY);
        intent.putExtra("action", "reg_state");
        intent.putExtra("data", s);
        sendBroadcast(intent);
        if (s.indexOf("succesful") != -1) {
            String str = "音频:\n";
            PayloadType[] audioCodecs = mLinphoneCore.getAudioPayloadTypes();
            for (PayloadType payloadType : audioCodecs) {
                str += payloadType.getMimeType() + "-" + payloadType.getNormalBitrate() + "-" + payloadType.enabled() + "\n";
            }
            str += "\n视频:";

            for (PayloadType payloadType1 : mLinphoneCore.getVideoPayloadTypes()) {
                // if(payloadType1.getMimeType().equalsIgnoreCase("VP8")) {payloadType1.enable(false);}
                payloadType1.getMimeType().equalsIgnoreCase("H265");
                str += payloadType1.getMimeType() + "" + payloadType1.enabled() + "\n";
            }
            Intent intent1 = new Intent(LinphoneActivity.RECEIVE_MAIN_ACTIVITY);
            intent1.putExtra("action", "show_code");
            intent1.putExtra("data", str);
            sendBroadcast(intent1);
        }
        ;
    }

    //报告先前添加到LinphoneCore的朋友的状态更改。
    @Override
    public void onNotifyPresenceReceived(Core core, Friend friend) {

    }

    @Override
    public void onEcCalibrationAudioInit(Core core) {

    }

    //聊天消息回调原型。
    @Override
    public void onMessageReceived(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }

    @Override
    public void onEcCalibrationResult(Core core, EcCalibratorStatus ecCalibratorStatus, int i) {

    }

    //用于通知应用程序有关从网络收到的订阅
    @Override
    public void onSubscribeReceived(Core core, Event event, String s, Content content) {

    }

    //用于接收信息消息的回调原型。
    @Override
    public void onInfoReceived(Core core, Call call, InfoMessage infoMessage) {
        Log.e(">onInfoReceived", infoMessage.getContent().toString());
    }

    //聊天室标记为读回调。
    @Override
    public void onCallStatsUpdated(Core core, Call call, CallStats callStats) {

    }

    //当朋友列表从核心朋友列表中删除时，回调原型用于报告。
    @Override
    public void onFriendListRemoved(Core core, FriendList friendList) {

    }

    @Override
    public void onReferReceived(Core core, String s) {

    }

    //回调原型告诉解码的qrcode的结果。
    @Override
    public void onQrcodeFound(Core core, String s) {

    }

    //用于配置状态更改通知的回调原型。
    @Override
    public void onConfiguringStatus(Core core, ConfiguringState configuringState, String s) {

    }


    /**
     * 回调通知已创建新的LinphoneCall（传入或传出）。
     *
     * @param core
     * @param call-新创建的LinPhoneCall对象
     */
    @Override
    public void onCallCreated(Core core, Call call) {

    }

    //用于通知应用程序有关发布状态的更改。
    @Override
    public void onPublishStateChanged(Core core, Event event, PublishState publishState) {
        Log.e(">onInfoReceived", publishState + "");
    }

    /**
     * 呼叫加密改变了回叫。
     *
     * @param core
     * @param call-                                    更改加密的呼叫
     * @param b-                                       是否激活加密
     * @param s-一个authentication_token，目前仅为ZRTP加密类型设置。
     */
    @Override
    public void onCallEncryptionChanged(Core core, Call call, boolean b, String s) {

    }

    //正在撰写通知回调原型。
    @Override
    public void onIsComposingReceived(Core core, ChatRoom chatRoom) {

    }

    //聊天消息未解密回调原型。
    @Override
    public void onMessageReceivedUnableDecrypt(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {

    }

    //用于报告日志收集上载进度指示的回调原型。
    @Override
    public void onLogCollectionUploadProgressIndication(Core core, int i, int i1) {

    }

    //用于报告版本更新检查结果的回调原型。
    @Override
    public void onVersionUpdateCheckResultReceived(Core core, VersionUpdateCheckResult versionUpdateCheckResult, String s, String s1) {

    }

    @Override
    public void onEcCalibrationAudioUninit(Core core) {

    }

    //全球消息回调。
    @Override
    public void onGlobalStateChanged(Core core, GlobalState globalState, String s) {

    }

    /**
     * 用于报告日志收集上载状态更改的回调原型。
     *
     * @param core
     * @param logCollectionUploadState- 日志集合上载的状态
     * @param s-                        附加信息：出现错误时的错误消息，成功时上传文件的URL
     */
    @Override
    public void onLogCollectionUploadStateChanged(Core core, Core.LogCollectionUploadState logCollectionUploadState, String s) {

    }


    /**
     * 收到DTMF通知的回拨。
     *
     * @param core
     * @param call- 收到dtmf的呼叫
     * @param i     - dtmf的ascii代码
     */
    @Override
    public void onDtmfReceived(Core core, Call call, int i) {

    }

    /**
     * 注册
     *
     * @param domain-域名
     * @param username-用户名
     * @param password-密码
     * @param port-端口
     * @param transportType-传输类型
     */

    public void login(String domain, String username, String password, String port, TransportType transportType) throws Exception {
        if (mLinphoneCore == null) {
            return;
        }
        for (ProxyConfig linphoneProxyConfig : mLinphoneCore.getProxyConfigList()) {
            mLinphoneCore.removeProxyConfig(linphoneProxyConfig);
        }
        for (AuthInfo x : mLinphoneCore.getAuthInfoList()) {
            mLinphoneCore.removeAuthInfo(x);
        }

        AccountBuilder builder = new AccountBuilder(mLinphoneCore).
                setUsername(username)
                .setDomain(domain + ":" + port)
                .setHa1(null)
                .setUserId(username)
                .setDisplayName("")
                .setPassword(password);

        String prefix = null;

        builder.setTempAvpfEnabled(false);

        if (prefix != null) {
            builder.setPrefix(prefix);
        }
        String forcedProxy = "";
        if (!TextUtils.isEmpty(forcedProxy)) {
            builder.setProxy(forcedProxy).setOutboundProxy(true);
        }

        if (transportType != null) {
            builder.setTransport(transportType);
        }
        try {
            builder.saveNewAccount();
        } catch (CoreException e) {
            Log.e("tishi", ">>>>" + e.getMessage());
        }

    }

    public void call(String username, String host, boolean isVideoCall) {
        if (mLinphoneCore == null) {
            return;
        }
        Address address = mLinphoneCore.interpretUrl(username + "@" + host);//默认代理配置用于解析地址。
        address.setDisplayName(username);//在地址中显示的名字
        CallParams params = mLinphoneCore.createCallParams(null);//配置和LinphoneCall的当前状态初始化参数 。
        params.enableVideo(isVideoCall);//是否开启视频
        params.getReceivedVideoDefinition();
        params.getUsedAudioPayloadType();
        params.getUsedVideoPayloadType();

        Call call = mLinphoneCore.inviteAddressWithParams(address, params);
        if (call == null) {
            Log.e("lilin error", "Could not place call to " + username);
            return;
        }
    }

    //挂断
    public void hangUp() {
        if (mLinphoneCore == null) {
            return;
        }
        mLinphoneCore.terminateAllCalls();//终止所有通话
    }

    //扬声器
    public void enkuo() {
        if (mAudioManager == null) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(mAudioManager.isSpeakerphoneOn() ? false : true);
    }

    //切换摄像头
    public void switch_camera(){
     try {
       String currentDevice=getLc().getVideoDevice();
       String[] devices=getLc().getVideoDevicesList();
       int index=0;
       for (String d:devices){
           if (d.equals(currentDevice)){
               break;
           }
           index++;
       }

       String newDevice;
       if (index==1)newDevice=devices[0];
       else if (devices.length>1)newDevice=devices[1];
       else newDevice=devices[index];
       getLc().setVideoDevice(newDevice);

       Call call=getLc().getCurrentCall();
       if (call==null){
           org.linphone.core.tools.Log.w("[Call Manager] Trying to switch camera while not in call");
           return;
       }
       call.update(null);
     }catch (ArithmeticException ae){
         org.linphone.core.tools.Log.e("[Call Manager] [Video] Cannot switch camera: no camera");
     }
    }

   //接听电话
    public void jie() {
        if (mLinphoneCore == null) {
            return;
        }
        Call currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            CallParams params = mLinphoneCore.createCallParams(currentCall);
            if (currentCall.getRemoteParams().videoEnabled()) {
                params.enableVideo(true);
            }
            currentCall.acceptWithParams(params);
        }
    }


    public static class AccountBuilder {
        private Core lc;
        private String tempUsername;//用户名
        private String tempDisplayName;//显示名
        private String tempUserId;//用户ID
        private String tempPassword;//密码
        private String tempHa1;
        private String tempDomain;//ip地址
        private String tempProxy;
        private String tempRealm;
        private String tempPrefix;
        private boolean tempOutboundProxy;
        private String tempContactsParams;
        private String tempExpire;
        private TransportType tempTransport;
        private boolean tempAvpfEnabled = false;
        private int tempAvpfRRInterval = 0;
        private String tempQualityReportingCollector;
        private boolean tempQualityReportingEnabled = false;
        private int tempQualityReportingInterval = 0;
        private boolean tempEnabled = true;//是否注册
        private boolean tempNoDefault = false;


        public AccountBuilder(Core lc) {
            this.lc = lc;
        }

        public AccountBuilder setTransport(TransportType transport) {
            tempTransport = transport;
            return this;
        }

        public AccountBuilder setUsername(String tempUsername) {
            this.tempUsername = tempUsername;
            return this;
        }

        public AccountBuilder setDisplayName(String displayName) {
            tempDisplayName = displayName;
            return this;
        }

        public AccountBuilder setUserId(String tempUserId) {
            this.tempUserId = tempUserId;
            return this;
        }

        public AccountBuilder setPassword(String tempPassword) {
            this.tempPassword = tempPassword;
            return this;
        }

        public AccountBuilder setHa1(String tempHa1) {
            this.tempHa1 = tempHa1;
            return this;
        }

        public AccountBuilder setDomain(String tempDomain) {
            this.tempDomain = tempDomain;
            return this;
        }

        public AccountBuilder setProxy(String tempProxy) {
            this.tempProxy = tempProxy;
            return this;
        }

        public void setRealm(String tempRealm) {
            this.tempRealm = tempRealm;
        }

        public AccountBuilder setPrefix(String tempPrefix) {
            this.tempPrefix = tempPrefix;
            return this;
        }

        public AccountBuilder setOutboundProxy(boolean tempOutboundProxy) {
            this.tempOutboundProxy = tempOutboundProxy;
            return this;
        }

        public void setContactsParams(String tempContactsParams) {
            this.tempContactsParams = tempContactsParams;
        }

        public void setExpire(String tempExpire) {
            this.tempExpire = tempExpire;
        }

        public void setTempTransport(TransportType tempTransport) {
            this.tempTransport = tempTransport;
        }

        public AccountBuilder setTempAvpfEnabled(boolean tempAvpfEnabled) {
            this.tempAvpfEnabled = tempAvpfEnabled;
            return this;
        }

        public void setTempAvpfRRInterval(int tempAvpfRRInterval) {
            this.tempAvpfRRInterval = tempAvpfRRInterval;
        }

        public void setQualityReportingCollector(String tempQualityReportingCollector) {
            this.tempQualityReportingCollector = tempQualityReportingCollector;
        }

        public void setQualityReportingEnabled(boolean tempQualityReportingEnabled) {
            this.tempQualityReportingEnabled = tempQualityReportingEnabled;
        }

        public void setQualityReportingInterval(int tempQualityReportingInterval) {
            this.tempQualityReportingInterval = tempQualityReportingInterval;
        }

        public void setEnabled(boolean tempEnabled) {
            this.tempEnabled = tempEnabled;
        }

        public void setNoDefault(boolean tempNoDefault) {
            this.tempNoDefault = tempNoDefault;
        }

        /**
         * 创建新用户
         *
         * @throws CoreException
         */
        public void saveNewAccount() throws CoreException {
            if (tempUsername == null || tempUsername.length() < 1 || tempDomain == null || tempDomain.length() < 1) {
                Log.e("===", "Skipping account save: username or domain not provided");
                return;
            }
            String identity = "sip:" + tempUsername + "@" + tempDomain;
            String proxy = "sip:";
            if (tempProxy == null) {
                proxy += tempDomain;
            } else {
                if (!tempProxy.startsWith("sip:") && !tempProxy.startsWith("<sip:") && !tempProxy.startsWith("sips:") &&
                        !tempProxy.startsWith("<sips:")) {
                    proxy += tempDomain;
                } else {
                    proxy = tempDomain;
                }
            }

            Address proxyAddr = Factory.instance().createAddress(proxy);
            Address identityAdd = Factory.instance().createAddress(identity);

            if (proxyAddr == null || identityAdd == null) {
                throw new CoreException("Proxy or Identity address is null !");
            }

            if (tempDisplayName != null) {
                identityAdd.setDisplayName(tempDisplayName);//设置显示名称
            }

            if (tempTransport != null) {
                proxyAddr.setTransport(tempTransport);//设置传输方式
            }

            String route = tempOutboundProxy ? proxyAddr.asStringUriOnly() : null;

            ProxyConfig proxyConfig = lc.createProxyConfig();
            proxyConfig.setIdentityAddress(identityAdd);//设置标识地址
            proxyConfig.setServerAddr(proxyAddr.asStringUriOnly());//设置代理地址
            proxyConfig.setRoute(route);//设置sip路由列表
            proxyConfig.enableRegister(tempEnabled);//是否注册

            if (tempContactsParams != null) {
                proxyConfig.setContactUriParameters(tempContactsParams);//设置可选的联系参数，这些参数将添加到URI内的注册中发送的联系信息中。
                if (tempExpire != null)
                    proxyConfig.setExpires(Integer.parseInt(tempExpire));//设置注册到期时间（以秒为单位）
            }

            proxyConfig.setAvpfMode(tempAvpfEnabled ? AVPFMode.Enabled : AVPFMode.Disabled);//启用RTCP反馈（也称为AVPF配置文件）
            proxyConfig.setAvpfRrInterval(tempAvpfRRInterval);//使用AVPF / SAVPF时，设置常规RTCP报告之间的间隔。
            proxyConfig.enableQualityReporting(tempQualityReportingEnabled);//指示是否应根据RFC 6035存储呼叫期间的质量统计信息并将其发送到收集器。
            proxyConfig.setQualityReportingCollector(tempQualityReportingCollector);//使用质量报告时设置收集器端点的路由。
            /**
             *  设置使用质量报告时发送的2个间隔报告之间的间隔。
             *            如果呼叫超过间隔大小，则会向收集器发送间隔报告。在呼叫终止时，将发送剩余时段的会话报告。值必须为0（禁用）或正数。
             *tempQualityReportingInterval - 以秒为单位的间隔，0表示禁用间隔报告。
             */
            proxyConfig.setQualityReportingInterval(tempQualityReportingInterval);

            if (tempPrefix != null) {
                proxyConfig.setDialPrefix(tempPrefix);//设置拨号前缀，以便在邀请号码时自动添加前缀 Core.invite(); 此拨号前缀通常应为用户所在国家/地区的国家/地区代码，不带“+”。
            }

            if (tempRealm != null) {
                proxyConfig.setRealm(tempRealm);//设置给定代理配置的领域。
            }

            AuthInfo authInfo = Factory.instance().createAuthInfo(tempUsername, tempUserId, tempPassword, tempHa1
                    , tempRealm, tempDomain);

            lc.addProxyConfig(proxyConfig);//添加代理配置
            lc.addAuthInfo(authInfo);//添加身份验证信息

            if (!tempNoDefault)
                lc.setDefaultProxyConfig(proxyConfig);//设置默认代理。

        }
    }
}




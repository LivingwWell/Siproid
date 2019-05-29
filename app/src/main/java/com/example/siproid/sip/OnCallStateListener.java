package com.example.siproid.sip;

public interface OnCallStateListener {
    /***
     * 正在呼出
     */
    void calling();

    /***
     * 对象响铃
     */
    void early();

    /***
     * 连接成功
     */
    void conmecting();

    /***
     * 通话中
     */
    void confirmed();

    /***
     * 挂断
     */
    void disconnected();

    /***
     * 通话失败
     */
    void error();
}

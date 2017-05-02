package com.wuxl.design.server;

/**
 * Created by wuxingle on 2017/5/1 0001.
 * 选项参数
 */
public class NIOServerOptions {

    interface NIOServerOption<T>{}

    public static final NIOServerOption<Integer> BIND_PORT = new NIOServerOption<Integer>() {};

    //心跳检测
    public static final NIOServerOption<Boolean> HT_START = new NIOServerOption<Boolean>() {};

    public static final NIOServerOption<Integer> HT_PERIOD = new NIOServerOption<Integer>() {};


}

package com.wuxl.design.protocol;

/**
 * Created by wuxingle on 2017/4/9 0009.
 * 数据传输协议
 * 来源    目的    命令   数据
 *  48      48     8      0-128
 *  6       6      1      0-16       bytes
 */
public class DataProtocol {

    public static final int TARGET_LENGTH = 6;

    public static final int ORIGIN_LENGTH = 6;

    public static final int DATA_LENGTH = 16;

    //数据包大小
    public static final int PACKET_MIN_LENGTH = 14;

    public static final int PACKET_MAX_LENGTH = 30;

    //结束位
    public static final byte DATA_END = 0x0a;

    //命令
    public static final byte OK = 0x11;

    public static final byte IS_APP = 0x21;

    public static final byte IS_MCU = 0x22;

    public static final byte ONLINE = 0x31;

    public static final byte ADD_LED = 0x41;

    public static final byte UPING = 0x51;

    public static final byte DOWNING = 0x52;

    public static final byte ON = 0x61;

    public static final byte OFF = 0x62;

    public static final byte PWM = 0x63;
}













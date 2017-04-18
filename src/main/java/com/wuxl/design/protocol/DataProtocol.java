package com.wuxl.design.protocol;

/**
 * 数据传输协议
 * 来源    目的    数据
 *  48      48     32
 *  6       6      4    bytes
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataProtocol {

    public static final int ORIGIN_LENGTH = 6;

    public static final int TARGET_LENGTH = 6;

    public static final int DATA_LENGTH = 4;

    public static final int RECEIVE_LENGTH = ORIGIN_LENGTH + TARGET_LENGTH + DATA_LENGTH;

    public static final int SEND_LENGTH = ORIGIN_LENGTH + DATA_LENGTH;

}

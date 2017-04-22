package com.wuxl.design.protocol;

/**
 * Created by wuxingle on 2017/4/9 0009.
 * 数据传输协议
 * 来源    目的    数据
 *  48      48     32
 *  6       6      4    bytes
 */
public class DataProtocol {

    public static final int ORIGIN_LENGTH = 6;

    public static final int TARGET_LENGTH = 6;

    public static final int DATA_LENGTH = 4;


}

package com.wuxl.design.protocol;

/**
 * 数据传输协议
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataProtocol {

    public static final int ID_LENGTH = 16;

    public static final int TYPE_POSITION = ID_LENGTH;
    public static final int TYPE_LENGTH = 1;

    public static final int CMD_POSITION = TYPE_POSITION + TYPE_LENGTH;
    public static final int CMD_LENGTH = 1;

    public static final int DATA_POSITION = CMD_POSITION + CMD_LENGTH;
    public static final int DATA_LENGTH = 1;

    public static final int TOTAL_LENGTH = ID_LENGTH + TYPE_LENGTH + CMD_LENGTH + DATA_LENGTH;

    //cmd
    public static final byte CMD_MY = (byte) 0x11;

    //type
    public static final byte TYPE_MCU = (byte) 0x8f;

    public static final byte TYPE_APP = (byte) 0xaa;


}

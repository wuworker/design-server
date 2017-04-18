package com.wuxl.design.protocol;

import javax.sql.DataSource;
import java.util.Arrays;

import static com.wuxl.design.common.DataUtils.toByte;
import static com.wuxl.design.common.DataUtils.toHex;
import static com.wuxl.design.common.DataUtils.toInteger;
import static com.wuxl.design.protocol.DataProtocol.*;

/**
 * 数据包
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataPackage {

    //数据来源
    private byte[] origin = new byte[ORIGIN_LENGTH];

    //数据目的地
    private byte[] target = new byte[TARGET_LENGTH];

    //其他数据
    private byte[] data = new byte[DATA_LENGTH];

    public DataPackage() {}

    public DataPackage(byte[] origin,byte[] target,byte[] data) {
        if(origin == null || origin.length < ORIGIN_LENGTH
                || target == null || target.length < TARGET_LENGTH
                || data == null || data.length < DATA_LENGTH){
            throw new IllegalArgumentException("数据包构造异常");
        }
        this.origin = Arrays.copyOf(origin,ORIGIN_LENGTH);
        this.target = Arrays.copyOf(target,TARGET_LENGTH);
        this.data = Arrays.copyOf(data, DATA_LENGTH);
    }

    public int getOriginLength(){
        return origin.length;
    }

    public int getTargetLength(){
        return target.length;
    }

    public int getDataLength(){
        return data.length;
    }

    public int getReceiveLength(){
        return getOriginLength() + getTargetLength() + getDataLength();
    }

    public int getSendLength(){
        return getOriginLength() + getDataLength();
    }

    public byte[] getOrigin() {
        return origin;
    }

    public byte[] getTarget() {
        return target;
    }

    public byte[] getData() {
        return data;
    }

    public String getHexOrigin(){
        return toHex(origin);
    }

    public String getHexTarget(){
        return toHex(target);
    }

    @Override
    public String toString() {
        return "DataPackage{" +
                "origin=" + getHexOrigin() +
                ", target=" + getHexTarget() +
                ", data=" + Arrays.toString(data) +
                '}';
    }

}

package com.wuxl.design.protocol;

import java.util.Arrays;

import static com.wuxl.design.common.DataUtils.toHex;
import static com.wuxl.design.protocol.DataProtocol.*;

/**
 * 数据包
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataPackage {

    //数据目的地
    private byte[] target = new byte[TARGET_LENGTH];

    //数据来源
    private byte[] origin = new byte[ORIGIN_LENGTH];

    //数据命令
    private byte cmd;

    //其他数据
    private byte[] data = new byte[DATA_LENGTH];

    private int dataLen;

    public DataPackage() {}

    public byte[] getOrigin() {
        return origin;
    }

    public byte[] getTarget() {
        return target;
    }

    public byte[] getData() {
        return data;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setTarget(byte[] target) {
        if(target == null){
            return;
        }
        if(target.length < TARGET_LENGTH){
            System.arraycopy(target,0,this.target,0,target.length);
        }else {
            System.arraycopy(target,0,this.target,0,TARGET_LENGTH);
        }
    }

    public void setOrigin(byte[] origin) {
        if(origin == null){
            return;
        }
        if(origin.length < ORIGIN_LENGTH){
            System.arraycopy(origin,0,this.origin,0,origin.length);
        }else {
            System.arraycopy(origin,0,this.origin,0,ORIGIN_LENGTH);
        }
    }

    public void setData(byte[] data) {
        if(data == null){
            return;
        }
        if(data.length < DATA_LENGTH){
            dataLen = data.length;
            System.arraycopy(data,0,this.data,0,data.length);
        }else {
            dataLen = DATA_LENGTH;
            System.arraycopy(data,0,this.data,0,DATA_LENGTH);
        }
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public String getHexOrigin(){
        return toHex(origin);
    }

    public String getHexTarget(){
        return toHex(target);
    }

    public void clear(){
        setDataLen(0);
    }

    @Override
    public String toString() {
        return "DataPackage{" +
                "target=" + getHexTarget() +
                ",origin=" + getHexOrigin() +
                ", cmd=" + cmd +
                ", data=" + Arrays.toString(Arrays.copyOf(data,dataLen)) +
                '}';
    }

}

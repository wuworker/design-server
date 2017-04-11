package com.wuxl.design.protocol;

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

    //数据目的
    private byte[] target = new byte[TARGET_LENGTH];

    private int data;

    public DataPackage() {}

    public DataPackage(byte[] origin,byte[] target,int data) {
        if(origin == null || origin.length < ORIGIN_LENGTH
                || target == null || target.length < TARGET_LENGTH){
            throw new IllegalArgumentException("数据包构造异常");
        }
        System.arraycopy(origin,0,this.origin,0,this.origin.length);
        System.arraycopy(target,0,this.target,0,this.target.length);
        this.data = data;
    }

    /**
     * 数据接收解析
     */
    public boolean receive(byte[] bytes){
        if(bytes == null || bytes.length < RECEIVE_LENGTH) {
            return false;
        }
        System.arraycopy(bytes,0,origin,0,origin.length);
        System.arraycopy(bytes,ORIGIN_LENGTH,target,0,target.length);
        data = toInteger(bytes,ORIGIN_LENGTH + TARGET_LENGTH);
        return true;
    }

    /**
     * 设置发送数据
     */
    public void setSendData(DataPackage data){
        byte[] bytes = data.getAllData();
        System.arraycopy(bytes,0,origin,0,origin.length);
        System.arraycopy(bytes,ORIGIN_LENGTH,target,0,target.length);
        this.data = toInteger(bytes,ORIGIN_LENGTH + TARGET_LENGTH);
    }

    /**
     * 获得发送数据
     * 只用发送来源and数据
     */
    public byte[] getSendData(){
        byte[] sendData = new byte[SEND_LENGTH];
        System.arraycopy(origin,0,sendData,0,origin.length);
        toByte(sendData,data,ORIGIN_LENGTH);

        return sendData;
    }

    /**
     * 获得所有数据
     */
    public byte[] getAllData(){
        byte[] all = new byte[RECEIVE_LENGTH];
        System.arraycopy(origin,0,all,0,origin.length);
        System.arraycopy(target,0,all,ORIGIN_LENGTH,target.length);
        toByte(all,data,ORIGIN_LENGTH + TARGET_LENGTH);

        return all;
    }

    public void setTarget(byte[] bytes){
        if(bytes==null || bytes.length < TARGET_LENGTH){
            throw  new IllegalArgumentException("参数不合法:"+ Arrays.toString(bytes));
        }
        System.arraycopy(bytes,0,target,0,target.length);
    }

    public byte[] getOrigin(){
        return origin;
    }

    public byte[] getTarget(){
        return target;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataPackage{" +
                "origin=" + toHex(origin) +
                ", target=" + toHex(target) +
                ", data=" + Integer.toHexString(data) +
                '}';
    }
}

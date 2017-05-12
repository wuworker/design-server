package com.wuxl.design.client;

import com.wuxl.design.client.impl.DefaultClient;
import com.wuxl.design.common.DataUtils;
import com.wuxl.design.protocol.DataProtocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by wuxingle on 2017/4/30 0030.
 * 客户端
 */
public abstract class AbstractClient {

    private boolean hasData;

    protected ByteBuffer buffer;

    protected byte[] origin;

    private String hexOrigin;

    private String address;

    private boolean online;

    //类型
    private int type;

    public AbstractClient(String address){
        this.address = address;
    }

    public static AbstractClient getDefaultClient(String address){
        return new DefaultClient(address);
    }

    public void setData(byte[] data){
        buffer.put(data);
        hasData = true;
    }

    public boolean hasData(){
        return hasData;
    }

    public ByteBuffer getBuffer(){
        return buffer;
    }

    public void clear(){
        buffer.clear();
        hasData = false;
    }

    public void setOrigin(byte[] origin){
        System.arraycopy(origin,0,this.origin,0,this.origin.length);
        hexOrigin = DataUtils.toHex(this.origin);
    }

    public byte[] getOrigin(){
        return Arrays.copyOf(origin,origin.length);
    }

    public String getAddress(){
        return address;
    }

    public String getHexOrigin(){
        return hexOrigin;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public synchronized boolean isOnline() {
        return online;
    }

    public synchronized void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return  (type == DataProtocol.IS_APP ? "app" : "mcu") +
                "{" +
                "origin=" + hexOrigin +
                ", address='" + getAddress() + '\'' +
                '}';
    }
}

package com.wuxl.design.protocol;

import com.wuxl.design.common.CommonUtils;

import java.nio.ByteBuffer;

import static com.wuxl.design.protocol.DataProtocol.*;

/**
 * 数据包
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataPackage {

    private byte[] id = new byte[ID_LENGTH];

    private byte type;

    private byte cmd;

    private byte data;

    private ByteBuffer buffer = ByteBuffer.allocate(32);

    public DataPackage() {}

    public DataPackage(byte[] bytes){
        setSendData(bytes);
    }

    public DataPackage(byte[] id, byte type, byte cmd, byte data) {
        System.arraycopy(id,0,this.id,0,this.id.length);
        this.type = type;
        this.cmd = cmd;
        this.data = data;
    }

    /**
     * 设置发送数据
     */
    public void setSendData(DataPackage data){
        setSendData(data.getSendData());
    }

    /**
     * 设置发送数据
     */
    public void setSendData(byte[] bytes){
        if(bytes == null || bytes.length < TOTAL_LENGTH)
            throw new IllegalArgumentException("数据包构造异常");
        System.arraycopy(bytes,0,id,0,id.length);
        type = bytes[TYPE_POSITION];
        cmd = bytes[CMD_POSITION];
        data = bytes[DATA_POSITION];
    }

    /**
     * 获得发送数据
     */
    public byte[] getSendData(){
        byte[] sendData = new byte[TOTAL_LENGTH];
        System.arraycopy(id,0,sendData,0,id.length);
        sendData[TYPE_POSITION] = type;
        sendData[CMD_POSITION] = cmd;
        sendData[DATA_POSITION] = data;

        return sendData;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte getData() {
        return data;
    }

    public void setData(byte data) {
        this.data = data;
    }

    public String getHexId(){
        return CommonUtils.toHex(id);
    }

    @Override
    public String toString() {
        return "DataPackage{" +
                "id=" + getHexId() +
                ", type=" + type +
                ", cmd=" + cmd +
                ", data=" + data +
                '}';
    }
}

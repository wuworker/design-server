package com.wuxl.design.client.impl;

import com.wuxl.design.client.NIOClient;
import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.wuxl.design.common.DataUtils.toHex;
import static com.wuxl.design.protocol.DataProtocol.ORIGIN_LENGTH;
import static com.wuxl.design.protocol.DataProtocol.TARGET_LENGTH;

/**
 * 客户端
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOClientImpl implements NIOClient{

    private static final Logger log = LoggerFactory.getLogger(NIOClientImpl.class);

    private static final  byte[] EMPTY_TARGET = new byte[TARGET_LENGTH];

    private byte[] origin = new byte[ORIGIN_LENGTH];

    private byte[] target = new byte[TARGET_LENGTH];

    //需要转发的数据
    private byte[] forwardData;

    private boolean hasData;

    private boolean shouldForward;

    private ByteBuffer buffer = ByteBuffer.allocate(32);

    public NIOClientImpl(){

    }

    /**
     * 数据处理
     */
    @Override
    public boolean process(DataExecutor dataExecutor) {
        shouldForward = false;

        buffer.flip();
        byte[] arrays = new byte[buffer.limit()];
        buffer.get(arrays,0,buffer.limit());
        log.info("{}[]receive data is:{}",this,Arrays.toString(arrays));
        buffer.clear();

        DataPackage dataPackage = dataExecutor.toDataPackage(arrays);
        if(dataPackage == null){
            log.warn("data parse fail");
            return false;
        }
        log.info("parse result:{}",dataPackage);

        byte[] origin = dataPackage.getOrigin();
        //设置来源
        if(Arrays.equals(this.origin,EMPTY_TARGET)){
            System.arraycopy(origin,0,this.origin,0,ORIGIN_LENGTH);
        }
        if(Arrays.equals(dataPackage.getTarget(),EMPTY_TARGET)){
            log.info("target is empty,can not forward[]{}",this);
            return true;
        }
        //设置需要转发的数据
        target = dataPackage.getTarget();
        forwardData = dataExecutor.formDataPackage(dataPackage);
        shouldForward = true;
        log.debug("set forward data :{}",Arrays.toString(forwardData));
        return true;
    }

    /**
     * 设置发送数据
     * @param data data
     */
    @Override
    public void setSendData(byte[] data){
        buffer.clear();
        buffer.put(data);
        buffer.flip();
        hasData = true;
    }

    /**
     * 获得需要转发数据
     * @return forward data
     */
    @Override
    public byte[] getForwardData(){
        return forwardData;
    }

    @Override
    public void clear() {
        buffer.clear();
        hasData = false;
    }

    @Override
    public byte[] getOrigin() {
        return origin;
    }

    @Override
    public byte[] getTarget() {
        return target;
    }

    @Override
    public boolean hasData() {
        return hasData;
    }

    @Override
    public boolean shouldForward() {
        return shouldForward;
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String getHexOrigin(){
        return toHex(origin);
    }

    @Override
    public String getHexTarget(){
        return toHex(target);
    }

    @Override
    public String toString() {
        return "NIOClient{" +
                "origin=" + getHexOrigin() +
                '}';
    }
}

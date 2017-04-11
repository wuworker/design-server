package com.wuxl.design.client.impl;

import com.wuxl.design.client.NIOClient;
import com.wuxl.design.protocol.DataPackage;

import java.util.Arrays;

import static com.wuxl.design.common.DataUtils.toHex;
import static com.wuxl.design.protocol.DataProtocol.ORIGIN_LENGTH;
import static com.wuxl.design.protocol.DataProtocol.TARGET_LENGTH;

/**
 * 客户端
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOClientImpl implements NIOClient{

    private static final  byte[] EMPTY_TARGET = new byte[TARGET_LENGTH];

    private byte[] origin = new byte[ORIGIN_LENGTH];

    private boolean hasData;

    private DataPackage dataPackage = new DataPackage();

    static{
        Arrays.fill(EMPTY_TARGET,(byte)0);
    }

    public NIOClientImpl(){}

    /**
     * 数据包处理
     *
     * @return 结果
     */
    @Override
    public boolean process() {

        byte[] target = dataPackage.getTarget();
        //设置来源
        if(Arrays.equals(target,EMPTY_TARGET)){
            System.arraycopy(dataPackage.getOrigin(),0,origin,0,origin.length);
            return false;
        }

        return true;
    }

    /**
     * 设置数据包
     *
     * @param dataPackage 数据包
     */
    @Override
    public void setDataPackage(DataPackage dataPackage) {
        this.dataPackage.setSendData(dataPackage);
    }

    /**
     * @return 获得数据包
     */
    @Override
    public DataPackage getDataPackage() {
        return dataPackage;
    }

    /**
     * 设置是否有数据
     */
    @Override
    public void setHasData() {
        hasData = true;
    }

    /**
     * @return 是否有数据
     */
    @Override
    public boolean hasData() {
        return hasData;
    }

    /**
     * 清空数据包
     */
    @Override
    public void clear() {
        hasData = false;
    }

    /**
     * 获得设备来源
     *
     * @return id
     */
    @Override
    public byte[] getOrigin() {
        return origin;
    }

    /**
     * 获得16进制的设备来源
     *
     * @return 来源16进制表示
     */
    @Override
    public String getHexOrigin() {
        return toHex(origin);
    }

    @Override
    public String toString() {
        return "Client{" +
                "origin=" + getHexOrigin() +
                '}';
    }
}

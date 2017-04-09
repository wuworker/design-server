package com.wuxl.design.client.impl;

import com.wuxl.design.client.NIOClient;
import com.wuxl.design.common.CommonUtils;
import com.wuxl.design.protocol.DataPackage;

import static com.wuxl.design.protocol.DataProtocol.CMD_MY;
import static com.wuxl.design.protocol.DataProtocol.ID_LENGTH;

/**
 * 客户端
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOClientImpl implements NIOClient{

    //设备id(mac)
    private byte[] id = new byte[ID_LENGTH];

    //类型
    private byte type = 0;

    //数据包
    private DataPackage dataPackage = new DataPackage();

    //是否有新数据
    private boolean hasData;

    public NIOClientImpl() {}

    /**
     * 数据包处理
     * @return 结果
     */
    @Override
    public boolean process() {

        byte cmd = dataPackage.getCmd();
        //自我设置
        if(cmd == CMD_MY){
            System.arraycopy(dataPackage.getId(),0,id,0,id.length);
            type = dataPackage.getType();
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
     * 清空数据包
     */
    @Override
    public void clear() {
        hasData = false;
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
     * 获得设备id
     *
     * @return id
     */
    @Override
    public byte[] getId() {
        return id;
    }

    /**
     * 获得16进制的id
     *
     * @return 16进制id
     */
    @Override
    public String getHexId() {
        return CommonUtils.toHex(id);
    }

    /**
     * 获得设备类型
     *
     * @return type
     */
    @Override
    public byte getType() {
        return type;
    }

    @Override
    public String toString() {
        return "设备{" +
                "id=" + getHexId() +
                ", type=" + type +
                '}';
    }
}

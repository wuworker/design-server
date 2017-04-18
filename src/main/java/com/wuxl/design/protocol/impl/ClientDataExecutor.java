package com.wuxl.design.protocol.impl;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by wuxingle on 2017/4/18.
 * 客户的数据解析器
 */
public class ClientDataExecutor extends DataExecutor{

    private static final Logger log = LoggerFactory.getLogger(ClientDataExecutor.class);

    /**
     * 客户的收到的数据格式为:
     * 来源   数据
     */
    @Override
    public DataPackage toDataPackage(byte[] bytes) {
        if(bytes==null || bytes.length < dataPackage.getOriginLength() + dataPackage.getDataLength()){
            log.warn("to DataPackage is error:{}", Arrays.toString(bytes));
            return null;
        }
        System.arraycopy(bytes,0,dataPackage.getOrigin(),0,dataPackage.getOriginLength());
        System.arraycopy(bytes,dataPackage.getOriginLength(),dataPackage.getData(),0,dataPackage.getDataLength());

        return dataPackage;
    }

    /**
     * 客户的发送的数据为
     * 来源  目的地  数据
     */
    @Override
    public byte[] formDataPackage(DataPackage dataPackage) {
        byte[] bytes = new byte[dataPackage.getOriginLength() + dataPackage.getTargetLength() + dataPackage.getDataLength()];
        System.arraycopy(dataPackage.getOrigin(),0,bytes,0,dataPackage.getOriginLength());
        System.arraycopy(dataPackage.getTarget(),0,bytes,dataPackage.getOriginLength(),dataPackage.getTargetLength());
        System.arraycopy(dataPackage.getData(),0,bytes,dataPackage.getOriginLength() + dataPackage.getTargetLength(),dataPackage.getDataLength());

        return bytes;
    }


}

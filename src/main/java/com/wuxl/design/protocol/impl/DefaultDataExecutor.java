package com.wuxl.design.protocol.impl;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.wuxl.design.protocol.DataProtocol.RECEIVE_LENGTH;

/**
 * Created by wuxingle on 2017/4/18.
 * 服务器的数据解析器
 *
 */
public class DefaultDataExecutor extends DataExecutor{

    private static final Logger log = LoggerFactory.getLogger(DefaultDataExecutor.class);

    /**
     * 接收解析
     */
    @Override
    public DataPackage toDataPackage(byte[] bytes) {
        if(bytes==null || bytes.length < dataPackage.getReceiveLength()){
            log.warn("to DataPackage is error:{}", Arrays.toString(bytes));
            return null;
        }
        System.arraycopy(bytes,0,dataPackage.getOrigin(),0,dataPackage.getOriginLength());
        System.arraycopy(bytes,dataPackage.getOriginLength(),dataPackage.getTarget(),0,dataPackage.getTargetLength());
        System.arraycopy(bytes,dataPackage.getOriginLength() + dataPackage.getTargetLength(),dataPackage.getData(),0,dataPackage.getDataLength());
        return dataPackage;
    }

    /**
     * 发送解析
     */
    @Override
    public byte[] formDataPackage(DataPackage dataPackage) {
        byte[] bytes = new byte[dataPackage.getSendLength()];
        System.arraycopy(dataPackage.getOrigin(),0,bytes,0,dataPackage.getOriginLength());
        System.arraycopy(dataPackage.getData(),0,bytes,dataPackage.getOriginLength(),dataPackage.getDataLength());

        return bytes;
    }


}

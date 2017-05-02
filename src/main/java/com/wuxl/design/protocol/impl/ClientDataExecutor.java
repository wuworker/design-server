package com.wuxl.design.protocol.impl;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;

/**
 * Created by wuxingle on 2017/4/18.
 * 客户的数据解析器
 */
public class ClientDataExecutor extends DataExecutor{

    /**
     * 客户的收到的数据格式为:
     * 来源   数据
     */
    @Override
    public DataPackage toDataPackage(byte[] bytes) {


        return dataPackage;
    }

    /**
     * 客户的发送的数据为
     * 来源  目的地  数据
     */
    @Override
    public byte[] fromDataPackage(DataPackage dataPackage) {

        return null;
    }


}

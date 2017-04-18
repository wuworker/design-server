package com.wuxl.design.protocol;

import com.wuxl.design.protocol.impl.ClientDataExecutor;
import com.wuxl.design.protocol.impl.DefaultDataExecutor;

/**
 * Created by wuxingle on 2017/4/18.
 * 数据解析器
 */
public abstract class DataExecutor {

    protected DataPackage dataPackage;

    public DataExecutor(){
        dataPackage = new DataPackage();
    }

    /**
     * 把读到的数据进行包装
     */
    public abstract DataPackage toDataPackage(byte[] bytes);

    /**
     * 把发送数据包转为byte
     */
    public abstract byte[] formDataPackage(DataPackage dataPackage);

    /**
     * 获得默认的解析器(服务端)
     */
    public static DataExecutor getDefaultDataExecutor(){
        return new DefaultDataExecutor();
    }

    /**
     * 获得客户端的数据解析器
     */
    public static DataExecutor getClientDataEecutor(){
        return new ClientDataExecutor();
    }

}

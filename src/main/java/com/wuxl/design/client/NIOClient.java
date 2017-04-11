package com.wuxl.design.client;

import com.wuxl.design.protocol.DataPackage;

/**
 * 客户端接口
 * Created by wuxingle on 2017/4/9 0009.
 */
public interface NIOClient {

    /**
     * 数据包处理
     * @return 结果
     */
    boolean process();

    /**
     * 设置数据包
     */
    void setDataPackage(DataPackage dataPackage);

    /**
     * @return 获得数据包
     */
    DataPackage getDataPackage();

    /**
     * 设置是否有数据
     */
    void setHasData();

    /**
     * @return 是否有数据
     */
    boolean hasData();

    /**
     * 清空数据包
     */
    void clear();

    /**
     * 获得设备来源
     * @return id
     */
    byte[] getOrigin();

    /**
     * 获得16进制的设备来源
     * @return
     */
    String getHexOrigin();

}

package com.wuxl.design.client;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;

import java.nio.ByteBuffer;

/**
 * 客户端接口
 * Created by wuxingle on 2017/4/9 0009.
 */
public interface NIOClient {

    /**
     * 数据处理
     * @param dataExecutor 数据解析器
     * @return 结果
     */
    boolean process(DataExecutor dataExecutor);

    /**
     * 设置需要发送的数据
     * @param data data
     */
    void setSendData(byte[] data);

    /**
     * 获得需要转发的数据
     * @return data
     */
    byte[] getForwardData();

    /**
     * 是否需要转发
     */
    boolean shouldForward();

    /**
     * 是否需要发送
     */
    boolean hasData();

    /**
     * 清空缓冲区
     */
    void clear();

    /**
     * 获得设备来源
     */
    byte[] getOrigin();

    String getHexOrigin();

    /**
     * 获得设备目的地
     */
    byte[] getTarget();

    String getHexTarget();

    /**
     * 拿到缓冲区
     * @return buffer
     */
    ByteBuffer getBuffer();


}

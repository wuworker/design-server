package com.wuxl.design.protocol.impl;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;

import static com.wuxl.design.protocol.DataProtocol.*;

/**
 * Created by wuxingle on 2017/4/18.
 * 默认的数据解析器
 */
public class DefaultDataExecutor extends DataExecutor {

    /**
     * 接收解析
     */
    @Override
    public DataPackage toDataPackage(byte[] bytes) {
        if (bytes == null || bytes.length < PACKET_MIN_LENGTH) {
            return null;
        }

        System.arraycopy(bytes, 0, dataPackage.getTarget(), 0, TARGET_LENGTH);
        System.arraycopy(bytes, TARGET_LENGTH, dataPackage.getOrigin(), 0, ORIGIN_LENGTH);
        dataPackage.setCmd(bytes[TARGET_LENGTH + ORIGIN_LENGTH]);
        int dataStart = TARGET_LENGTH + ORIGIN_LENGTH + 1;
        //去掉数据尾
        int dataLen = bytes.length - dataStart - 1;
        System.arraycopy(bytes, dataStart, dataPackage.getData(), 0, dataLen);
        dataPackage.setDataLen(dataLen);
        return dataPackage;
    }

    /**
     * 发送解析
     */
    @Override
    public byte[] fromDataPackage(DataPackage dataPackage) {
        byte[] bytes = new byte[PACKET_MIN_LENGTH + dataPackage.getDataLen()];
        System.arraycopy(dataPackage.getTarget(), 0, bytes, 0, TARGET_LENGTH);
        System.arraycopy(dataPackage.getOrigin(), 0, bytes, TARGET_LENGTH, ORIGIN_LENGTH);
        bytes[TARGET_LENGTH + ORIGIN_LENGTH] = dataPackage.getCmd();
        System.arraycopy(dataPackage.getData(), 0,
                bytes, TARGET_LENGTH + ORIGIN_LENGTH + 1,
                dataPackage.getDataLen());
        //增加数据尾
        bytes[bytes.length - 1] = DATA_END;
        return bytes;
    }


}

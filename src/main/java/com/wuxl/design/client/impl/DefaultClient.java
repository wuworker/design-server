package com.wuxl.design.client.impl;

import com.wuxl.design.client.AbstractClient;

import java.nio.ByteBuffer;

import static com.wuxl.design.protocol.DataProtocol.ORIGIN_LENGTH;

/**
 * Created by wuxingle on 2017/4/30 0030.
 * 默认设备
 */
public class DefaultClient extends AbstractClient{

    public DefaultClient(String address){
        super(address);
        buffer = ByteBuffer.allocate(64);
        origin = new byte[ORIGIN_LENGTH];
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof AbstractClient){
            AbstractClient client = (AbstractClient)obj;
            return client.getHexOrigin().equals(getHexOrigin());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getHexOrigin().hashCode();
    }

}

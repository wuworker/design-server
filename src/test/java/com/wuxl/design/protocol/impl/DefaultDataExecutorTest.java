package com.wuxl.design.protocol.impl;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by wuxingle on 2017/4/30 0030.
 * 数据解析的测试
 */
public class DefaultDataExecutorTest {

    @Test
    public void testDataExecutor(){
        DataExecutor dataExecutor = DataExecutor.getDefaultDataExecutor();
        dataExecutor.setDataPackage(new DataPackage());
        byte[] data = {
                0x12,0x12,0x12,0x12,0x12,0x12,  //target
                0x34,0x34,0x34,0x34,0x34,0x34, //origin
                0x56,                         //cmd
                0x78,0x79,0x0a                //data
        };
        DataPackage dataPackage = dataExecutor.toDataPackage(data);
        System.out.println(dataPackage);

        byte[] result = dataExecutor.fromDataPackage(dataPackage);
        System.out.println(Arrays.toString(result));

        Assert.assertTrue(Arrays.equals(data,result));
    }



}
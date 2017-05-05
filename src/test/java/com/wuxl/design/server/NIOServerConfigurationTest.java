package com.wuxl.design.server;

import org.dom4j.DocumentException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by wuxingle on 2017/5/5.
 * 读取xml文件测试
 */
public class NIOServerConfigurationTest {

    @Test
    public void testConfigNotFile(){
        try {
            NIOServerConfiguration config = new NIOServerConfiguration();
            Map<NIOServerOption,String> options = config.getOptions();
            for(NIOServerOption option:options.keySet()){
                System.out.println(option+":"+options.get(option));
            }
        }catch (DocumentException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testConfigFile(){
        try {
            NIOServerConfiguration config = new NIOServerConfiguration("src/test/resources/design-server-test.xml");
            Map<NIOServerOption,String> options = config.getOptions();
            for(NIOServerOption option:options.keySet()){
                System.out.println(option+"-->"+options.get(option));
            }
        }catch (DocumentException e){
            e.printStackTrace();
        }
    }
}
package com.wuxl.design.common;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 *
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataUtilsTest {

    @Test
    public void toHex() throws Exception {

        byte[] bytes = {127,110,20,(byte)0xff};
        String hex = DataUtils.toHex(bytes);
        System.out.println(hex);

    }

    @Test
    public void toInteger(){
        byte[] bytes = {
                0x00,(byte)0xfa,0x74,(byte)0xc6
        };
        int num = DataUtils.toInteger(bytes);
        System.out.println(num);
        System.out.println(Integer.toHexString(num));
        byte[] newBytes = new byte[4];
        DataUtils.toByte(newBytes,num,0);
        System.out.println(Arrays.toString(newBytes));
        assertTrue(Arrays.equals(newBytes,bytes));
    }

}
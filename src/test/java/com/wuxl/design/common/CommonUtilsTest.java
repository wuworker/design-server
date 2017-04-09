package com.wuxl.design.common;

import org.junit.Test;

/**
 *
 * Created by wuxingle on 2017/4/9 0009.
 */
public class CommonUtilsTest {

    @Test
    public void toHex() throws Exception {

        byte[] bytes = {127,110,20,(byte)0xff};
        String hex = CommonUtils.toHex(bytes);
        System.out.println(hex);

    }

}
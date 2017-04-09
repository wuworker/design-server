package com.wuxl.design.common;

/**
 * 公共的工具类
 * Created by wuxingle on 2017/4/9 0009.
 */
public class CommonUtils {

    /**
     * 转为16进制字符串
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    public static String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        char[] chars = {'0','1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(chars[(b & 0xff) >> 4]);
            sb.append(chars[(b & 0x0f)]);
        }
        return sb.toString();
    }


}

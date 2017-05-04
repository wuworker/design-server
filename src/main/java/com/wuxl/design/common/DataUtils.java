package com.wuxl.design.common;

/**
 * 公共的工具类
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataUtils {

    private static final char[] HEX_CHARS = {
            '0','1', '2', '3', '4',
            '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * 转为16进制字符串
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    public static String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(HEX_CHARS[(b & 0xff) >> 4]);
            sb.append(HEX_CHARS[(b & 0x0f)]);
        }
        return sb.toString();
    }

    /**
     * 根据byte拿到int
     * byte[3] byte[2] byte[1] byte[0]
     * hh8    hl8     lh8     ll8
     */
    public static int toInteger(byte[] bytes) {
        return toInteger(bytes, 0);
    }

    public static int toInteger(byte[] bytes, int start) {
        if (bytes == null) {
            return 0;
        }
        int data = 0;
        for (int i = 0; start+i < bytes.length && i < 4; i++) {
            data |= ((bytes[start + i] & 0xff) << (i * 8));
        }
        return data;
    }

    /**
     * 转为byte
     * byte[3] byte[2] byte[1] byte[0]
     * hh8    hl8     lh8     ll8
     */
    public static byte[] toByte(int num) {
        byte[] bytes = new byte[4];
        toByte(bytes, num, 0);
        return bytes;
    }

    public static void toByte(byte[] bytes, int num, int start) {
        if (bytes == null) {
            return;
        }
        for (int i = 0; i + start < bytes.length && i < 4; i++) {
            bytes[start + i] = (byte) ((num >> (i * 8)) & 0xff);
        }

    }

    /**
     * 找出相同元素的索引
     */
    public static int indexOfByte(byte[] bytes,byte data){
        return indexOfByte(bytes,0,data);
    }

    public static int indexOfByte(byte[] bytes,int start,byte data){
        if(bytes==null){
            return -1;
        }
        for(int i=start;i<bytes.length;i++){
            if(bytes[i]==data){
                return i;
            }
        }
        return -1;
    }


}

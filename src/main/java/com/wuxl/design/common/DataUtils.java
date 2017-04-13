package com.wuxl.design.common;

/**
 * 公共的工具类
 * Created by wuxingle on 2017/4/9 0009.
 */
public class DataUtils {

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

    /**
     * 根据byte拿到int
     * byte[0] byte[1] byte[2] byte[3]
     *   hh8    hl8     lh8     ll8
     */
    public static int toInteger(byte[] bytes){
        return toInteger(bytes,0);
    }

    public static int toInteger(byte[] bytes,int start){
        if (bytes == null) {
            return 0;
        }
        int data = 0;
        for(int i=0;i<bytes.length && i<4;i++){
            data |= ((bytes[start + i] & 0xff) << (3-i)*8);
        }
        return  data;
    }

    /**
     * 转为byte
     * byte[0] byte[1] byte[2] byte[3]
     *   hh8    hl8     lh8     ll8
     */
    public static byte[] toByte(int num){
        byte[] bytes = new byte[4];
        toByte(bytes,num,0);
        return bytes;
    }

    public static void toByte(byte[] bytes,int num,int start){
        if(bytes==null || bytes.length < start + 4){
            throw new IllegalArgumentException("参数不合法");
        }
        bytes[start] = (byte)((num >> 24) & 0xff);
        bytes[start+1] = (byte)((num >> 16) & 0xff);
        bytes[start+2] = (byte)((num >> 8) & 0xff);
        bytes[start+3] = (byte)((num) & 0xff);
    }


}

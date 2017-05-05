package com.wuxl.design.server;

/**
 * Created by wuxingle on 2017/5/5.
 *
 */
public enum NIOServerOption {

    BIND_PORT,
    HEART_START,
    HEART_PERIOD,
    ;





    public static NIOServerOption getByString(String name){
        switch (name) {
            case "port":
                return BIND_PORT;
            case "start":
                return HEART_START;
            case "period":
                return HEART_PERIOD;
            default:break;
        }
        return null;
    }

}

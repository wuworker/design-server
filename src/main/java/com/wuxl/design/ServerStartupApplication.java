package com.wuxl.design;

import com.wuxl.design.server.NIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务的启动类
 * Created by wuxingle on 2017/4/9 0009.
 */
public class ServerStartupApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerStartupApplication.class);

    public static void main(String[] args) {
        try {
            NIOServer server = NIOServer.getInstance();
            server.bind(9999).start();
        }catch (Exception e){
            log.error("server running error",e);
        }
    }

}

package com.wuxl.design.server;

import com.wuxl.design.client.AbstractClient;
import com.wuxl.design.common.DataUtils;
import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.wuxl.design.protocol.DataProtocol.*;
import static com.wuxl.design.server.NIOServerOptions.*;

/**
 * 服务器
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOServer {

    private static final Logger log = LoggerFactory.getLogger(NIOServer.class);

    private static NIOServer instance;

    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    //注册列表
    private Map<String, AbstractClient> registerList;

    //关联关系
    private Map<String, Set<String>> clientsMapping;

    private Selector selector;

    //数据解析者
    private DataExecutor dataExecutor;

    //参数设置
    private Map<NIOServerOptions.NIOServerOption,Object> options = new HashMap<>();

    private NIOServer() {
        registerList = Collections.synchronizedMap(new HashMap<>());
        clientsMapping = Collections.synchronizedMap(new HashMap<>());
        dataExecutor = DataExecutor.getDefaultDataExecutor();
        dataExecutor.setDataPackage(new DataPackage());

        //默认参数
        options.put(BIND_PORT,9999);
        options.put(HT_START,true);
        options.put(HT_PERIOD,300);
    }

    //获得实例
    public static NIOServer getInstance() {
        if (instance == null) {
            instance = new NIOServer();
        }
        return instance;
    }

    /**
     * 绑定端口号
     *
     * @return 服务实例
     */
    public NIOServer bind() throws IOException {
        int port = (int)options.get(BIND_PORT);
        try {
            instance.init(port);
            log.info("server bind port:{}", port);
        } catch (IOException e) {
            log.error("server bind error", e);
            throw e;
        }
        return instance;
    }

    /**
     * 设置参数
     */
    public <T> NIOServer setOptions(NIOServerOptions.NIOServerOption<T> name,T value){
        options.put(name,value);
        return instance;
    }

    /**
     * 开始运行
     */
    public void start() throws IOException {
        log.info("server start!");
        boolean optionOf_HT_START = (boolean)options.get(HT_START);
        int optionOf_HT_PERIOD = (int)options.get(HT_PERIOD);
        if(optionOf_HT_START){
            log.info("heartbeat is enable,period is {} seconds",optionOf_HT_PERIOD);
            scheduledExecutor.scheduleWithFixedDelay(new HeartbeatTask(),
                    optionOf_HT_PERIOD,optionOf_HT_PERIOD, TimeUnit.SECONDS);
        }
        instance.listen();
    }

    /**
     * 服务器初始化
     *
     * @param port 端口号
     */
    private void init(int port) throws IOException {
        selector = Selector.open();
        //创建服务
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 服务器开始监听
     */
    private void listen() throws IOException {
        int count;
        while (true) {
            try {
                count = selector.select();
            } catch (IOException e) {
                log.error("server select error", e);
                break;
            }
            if (count == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                handlerKey(key);
            }
        }
        log.info("server stop");
        selector.close();
    }

    /**
     * 事件处理
     */
    private void handlerKey(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                handlerAccept(key);
            } else if (key.isReadable()) {
                handlerReader(key);
            } else if (key.isWritable()) {
                handlerWriter(key);
            }
        } catch (RuntimeException e) {
            log.error("server runtime error", e);
        }
    }

    /**
     * 处理连接事件
     */
    private void handlerAccept(SelectionKey key) {
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector,
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            SocketAddress address = socketChannel.getRemoteAddress();
            AbstractClient client = AbstractClient.getDefaultClient(address.toString());
            selectionKey.attach(client);
            log.info("accept a new client :{}", address);
        } catch (IOException e) {
            log.error("client connected error", e);
            handlerIOException(key);
        }
    }

    /**
     * 处理读事件
     */
    private void handlerReader(SelectionKey key) {
        AbstractClient client = null;
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            client = (AbstractClient) key.attachment();
            //接收数据
            ByteBuffer buffer = client.getBuffer();
            int count = socketChannel.read(buffer);
            if (count != -1) {
                buffer.flip();
                byte[] arrays = new byte[count];
                buffer.get(arrays, 0, count);
                buffer.clear();
                log.info("receive count is {}", count);
                int start = 0;
                int end = DataUtils.indexOfByte(arrays, DATA_END);
                //说明有多个数据
                while ((end != -1) && (end++ <= arrays.length - 1)) {
                    DataPackage dataPackage = dataExecutor.toDataPackage(
                            Arrays.copyOfRange(arrays, start, end));
                    start = end;
                    end = DataUtils.indexOfByte(arrays, start, DATA_END);
                    if (dataPackage == null) {
                        log.info("parse data error:{}", Arrays.toString(arrays));
                        continue;
                    }
                    //数据处理
                    process(dataPackage, client);
                }
                log.info("handler reader over");
            } else {
                log.info("client is close[]{}", client);
                //删除设备
                delClient(client);
                handlerIOException(key);
            }
        } catch (IOException e) {
            log.info("client force close[]{}", client, e);
            delClient(client);
            handlerIOException(key);
        }
    }

    /**
     * 处理写事件
     *
     * @param key SelectionKey
     */
    private void handlerWriter(SelectionKey key) {
        AbstractClient client = null;
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            client = (AbstractClient) key.attachment();
            if (client.hasData()) {
                ByteBuffer buffer = client.getBuffer();
                buffer.flip();
                socketChannel.write(buffer);
                log.info("send data to {}", client);
                //清空数据
                client.clear();
            }
        } catch (IOException e) {
            log.error("send data error[]{}", client, e);
            delClient(client);
            handlerIOException(key);
        }
    }

    /**
     * IO异常处理
     *
     * @param key key
     */
    private void handlerIOException(SelectionKey key) {
        try {
            log.info("client count now is :{}", registerList.size());
            key.cancel();
            key.channel().close();
        } catch (IOException e) {
            log.error("key closing error", e);
        }
    }

    /**
     * 数据处理
     */
    private void process(DataPackage dataPackage, AbstractClient client) {
        log.info("[]--->[]process");
        switch (dataPackage.getCmd()) {
            //注册
            case IS_APP:
                client.setType(IS_APP);
                register(dataPackage, client);
                break;
            case IS_MCU:
                client.setType(IS_MCU);
                register(dataPackage, client);
                break;
            case ADD_LED:
                setMappings(dataPackage);
                break;
            default:
                forwardMessage(dataPackage);
                break;
        }
        log.info("[]<---[]process");
    }


    /**
     * 设置关联关系
     */
    private void setMappings(DataPackage dataPackage) {
        String led = dataPackage.getHexTarget();
        String app = dataPackage.getHexOrigin();
        Set<String> apps = clientsMapping.get(led);
        if (apps == null) {
            apps = new HashSet<>();
            apps.add(app);
            clientsMapping.put(led, apps);
        } else {
            apps.add(app);
        }
    }

    /**
     * 数据转发
     */
    private void forwardMessage(DataPackage dataPackage) {
        String hexTarget = dataPackage.getHexTarget();
        AbstractClient client = registerList.get(hexTarget);
        if (client != null) {
            client.setData(dataExecutor.fromDataPackage(dataPackage));
            log.info("forward message:{}", dataPackage);
        } else {
            log.info("this message don't find target:{}", dataPackage);
        }
    }

    /**
     * 设备注册
     */
    private void register(DataPackage dataPackage, AbstractClient client) {
        client.setOrigin(dataPackage.getOrigin());
        String hexOrigin = client.getHexOrigin();
        if (!registerList.containsKey(hexOrigin)) {
            registerList.put(hexOrigin, client);
            log.info("register a new client[]{}", client);
            log.info("the client count is {}",registerList.size());
            if (!clientsMapping.containsKey(hexOrigin)) {
                return;
            }
            //上线通知
            Set<String> apps = clientsMapping.get(hexOrigin);
            dataPackage.setCmd(UPING);
            Iterator<String> it = apps.iterator();
            while (it.hasNext()) {
                String app = it.next();
                AbstractClient appClient = registerList.get(app);
                //说明app已下线
                if (appClient == null) {
                    it.remove();
                    continue;
                }
                dataPackage.setTarget(appClient.getOrigin());
                forwardMessage(dataPackage);
                log.info("notify app the mcu is online[]{}", appClient);
            }
        } else {
            log.info("client already registry");
        }
    }

    /**
     * 删除设备
     *
     * @param client 要删除的设备
     */
    private void delClient(AbstractClient client) {
        String origin = client.getHexOrigin();
        if (!registerList.containsKey(origin)) {
            log.info("not find this client,maybe already deleted[]{}", client);
            return;
        }
        //删除设备
        registerList.remove(origin);
        if (!clientsMapping.containsKey(origin)) {
            return;
        }
        //下线通知
        Set<String> apps = clientsMapping.get(origin);
        DataPackage dataPackage = new DataPackage();
        dataPackage.setOrigin(client.getOrigin());
        dataPackage.setCmd(DOWNING);
        Iterator<String> it = apps.iterator();
        while (it.hasNext()) {
            String app = it.next();
            AbstractClient appClient = registerList.get(app);
            //说明app已下线
            if (appClient == null) {
                it.remove();
                continue;
            }
            dataPackage.setTarget(appClient.getOrigin());
            forwardMessage(dataPackage);
            log.info("notify app the mcu is down[]{}", appClient);
        }
    }

    /**
     * 心跳检测
     */
    private class HeartbeatTask implements Runnable{

        private final byte[] HEARTBEAT_PACKET = {(byte)0xff,0x0a};

        @Override
        public void run() {
            log.info("send to heartbeat packet");
            for(String origin : registerList.keySet()){
                AbstractClient client = registerList.get(origin);
                //如果是单片机需要定时心跳
                if(client.getType() == IS_MCU
                        && !client.hasData()){
                    client.setData(HEARTBEAT_PACKET);
                }
            }
        }
    }

}










package com.wuxl.design.server;

import com.wuxl.design.client.AbstractClient;
import com.wuxl.design.common.DataUtils;
import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;
import org.dom4j.DocumentException;
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

import static com.wuxl.design.protocol.DataProtocol.*;
import static com.wuxl.design.server.NIOServerOption.*;

/**
 * 服务器
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOServer {

    private static final Logger log = LoggerFactory.getLogger(NIOServer.class);

    private static NIOServer instance;

    private Timer timer  = new Timer(true);

    private Map<String,SelectionKey> socketMap;

    //注册列表
    private Map<String, AbstractClient> registerMap;

    //关联关系
    private Map<String, Set<String>> clientsMapping;

    private Selector selector;

    //数据解析者
    private DataExecutor dataExecutor;

    //参数设置
    private Map<NIOServerOption,String> options = new HashMap<>();

    private int mcuCount;

    private int appCount;

    private NIOServer(String configFile){
        socketMap = Collections.synchronizedMap(new HashMap<>());
        registerMap = Collections.synchronizedMap(new HashMap<>());
        clientsMapping = Collections.synchronizedMap(new HashMap<>());
        dataExecutor = DataExecutor.getDefaultDataExecutor();
        dataExecutor.setDataPackage(new DataPackage());

        try {
            NIOServerConfiguration configuration = new NIOServerConfiguration(configFile);
            options = configuration.getOptions();
        }catch (DocumentException e){
            log.error("parse xml error",e);
        }
    }

    //获得实例
    public static NIOServer getInstance() {
        return getInstance(null);
    }

    public static NIOServer getInstance(String configFile) {
        if (instance == null) {
            instance = new NIOServer(configFile);
        }
        return instance;
    }


    /**
     * 绑定端口号
     * @return 服务实例
     */
    public NIOServer bind() throws IOException,NumberFormatException {
        try {
            int port = Integer.parseInt(options.get(BIND_PORT));
            instance.init(port);
        } catch (IOException e) {
            log.error("server bind error", e);
            throw e;
        } catch (NumberFormatException e){
            log.error("port is not a number");
            throw e;
        }
        return instance;
    }

    /**
     * 设置参数
     */
    public NIOServer setOptions(NIOServerOption name, String value){
        options.put(name,value);
        return instance;
    }

    /**
     * 开始运行
     */
    public void start() throws IOException,NumberFormatException{
        log.debug("server config is:");
        for(NIOServerOption option:options.keySet()){
            log.debug("{}--->{}",option,options.get(option));
        }

        log.info("server start!");
        boolean start;
        int period;
        try {
            start = Boolean.parseBoolean(options.get(HEART_START));
            period = Integer.parseInt(options.get(HEART_PERIOD));
        }catch (NumberFormatException e){
            log.error("heart param is error");
            throw e;
        }
        if(start){
            log.info("heartbeat is enable,period is {} seconds",period);
            timer.schedule(new HeartbeatTask(),period * 1000,period * 1000);
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
            socketMap.put(address.toString(),selectionKey);
            AbstractClient client = AbstractClient.getDefaultClient(address.toString());
            client.setOnline(true);
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
        if(key == null){
            log.info("handler Exception key is null");
            return;
        }
        try {
            log.info("client count now is :app={},mcu={}", appCount,mcuCount);
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
        log.info("process begin");
        switch (dataPackage.getCmd()) {
            //注册
            case IS_APP:
                client.setType(IS_APP);
                appCount++;
                register(dataPackage, client);
                break;
            case IS_MCU:
                client.setType(IS_MCU);
                mcuCount++;
                register(dataPackage, client);
                break;
            case ADD_LED:
                setMappings(dataPackage);
                break;
            //单片机的心跳
            case ONLINE:
                client.setOnline(true);
                break;
            default:
                forwardMessage(dataPackage);
                break;
        }
        log.info("process end");
    }


    /**
     * 设置关联关系
     */
    private void setMappings(DataPackage dataPackage) {
        log.info("set mappings");
        String led = dataPackage.getHexTarget();
        String app = dataPackage.getHexOrigin();
        if(clientsMapping.containsKey(led)){
            Set<String> apps = clientsMapping.get(led);
            apps.add(app);
        }else {
            Set<String> apps = new HashSet<>();
            apps.add(app);
            clientsMapping.put(led,apps);
        }
    }

    /**
     * 数据转发
     */
    private void forwardMessage(DataPackage dataPackage) {
        String hexTarget = dataPackage.getHexTarget();
        AbstractClient client = registerMap.get(hexTarget);
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
        if (!registerMap.containsKey(hexOrigin)) {
            registerMap.put(hexOrigin, client);
            log.info("register a new client[]{}", client);
            log.info("the client count is {}", registerMap.size());
            if (!clientsMapping.containsKey(hexOrigin)) {
                return;
            }
            //上线通知
            Set<String> apps = clientsMapping.get(hexOrigin);
            dataPackage.setCmd(UPING);
            Iterator<String> it = apps.iterator();
            while (it.hasNext()) {
                String app = it.next();
                AbstractClient appClient = registerMap.get(app);
                //说明app已下线
                if (appClient == null) {
                    it.remove();
                    continue;
                }
                dataPackage.setTarget(appClient.getOrigin());
                forwardMessage(dataPackage);
                log.info("notify app the mcu is online[]{}", appClient);
            }
            log.info("client count now is :app={},mcu={}", appCount,mcuCount);
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
        socketMap.remove(client.getAddress());
        if (!registerMap.containsKey(origin)) {
            log.info("not find this client,maybe already deleted[]{}", client);
            return;
        }
        //删除设备
        registerMap.remove(origin);
        if(client.getType() == IS_APP){
            appCount--;
        }else {
            mcuCount--;
        }
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
            AbstractClient appClient = registerMap.get(app);
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
     * 检测单片机是否掉线
     */
    private class HeartbeatTask extends TimerTask{
        List<AbstractClient> delList = new ArrayList<>();

        @Override
        public void run() {
            if(mcuCount == 0){
                return;
            }
            log.info("check mcu is online");
            for(String origin : registerMap.keySet()){
                AbstractClient client = registerMap.get(origin);
                if(client.getType()==IS_APP)
                    continue;
                //如果是单片机需要定时心跳
                if(client.isOnline()){
                    client.setOnline(false);
                }else {
                    delList.add(client);
                }
            }
            for(AbstractClient client:delList){
                log.info("{} is down",client);
                SelectionKey key = socketMap.get(client.getAddress());
                socketMap.remove(client.getAddress());
                SocketChannel socketChannel = (SocketChannel)key.channel();
                delClient(client);

                try {
                    key.cancel();
                    socketChannel.close();
                }catch (IOException e){
                    log.error("close useless mcu error");
                }
            }
            delList.clear();
        }
    }

}










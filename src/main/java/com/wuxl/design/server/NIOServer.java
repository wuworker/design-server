package com.wuxl.design.server;

import com.wuxl.design.client.NIOClient;
import com.wuxl.design.client.impl.NIOClientImpl;
import com.wuxl.design.protocol.DataExecutor;
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

/**
 * 服务器
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOServer {

    private static final Logger log = LoggerFactory.getLogger(NIOServer.class);

    private static NIOServer instance;

    //客户端列表
    private List<NIOClient> clients = new ArrayList<>();

    private Selector selector;

    private boolean running;

    //数据解析者
    private DataExecutor dataExecutor;

    //服务绑定的端口号
    private int port;

    private NIOServer() {
        dataExecutor = DataExecutor.getDefaultDataExecutor();
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
     * @param port 端口号
     * @return 服务实例
     */
    public NIOServer bind(int port) throws IOException {
        this.port = port;
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
     * 开始运行
     */
    public void start() throws IOException {
        log.info("server start");
        running = true;
        instance.listen();
    }

    /**
     * 关闭服务
     */
    public void close() {
        log.info("server closing...");
        running = false;
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
        int count = 0;
        while (running) {
            try {
                count = selector.select();
            }catch (IOException e){
                log.error("server select error",e);
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
        running = false;
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
        }catch (IOException e){
            log.error("socket close error",e);
        } catch (RuntimeException e){
            log.error("server runtime error",e);
        }
    }

    /**
     * 处理连接事件
     */
    private void handlerAccept(SelectionKey key)throws IOException{
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector,
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            NIOClient client = new NIOClientImpl();
            selectionKey.attach(client);
            SocketAddress address = socketChannel.getRemoteAddress();
            log.info("accept a new client :{}", address);
        }catch (IOException e){
            log.error("client connected error",e);
            key.cancel();
            key.channel().close();
        }
    }

     /**
     * 处理读事件
     */
    private void handlerReader(SelectionKey key)throws IOException{
        NIOClient client = null;
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            client = (NIOClient) key.attachment();
            //接收数据
            int count = socketChannel.read(client.getBuffer());
            if (count != -1) {
                boolean result = client.process(dataExecutor);
                if(!result){
                    log.warn("ignore this request");
                    return;
                }
                if(!client.shouldForward()){
                    //添加设备
                    addClient(client);
                    log.info("client count is :{}",clients.size());
                } else {
                    forwardMessage(client);
                }
            } else {
                log.info("client is close[]{}",client);
                //删除设备
                delClient(client);
                log.info("client count is :{}",clients.size());
                key.cancel();
                key.channel().close();
            }
        }catch (IOException e){
            log.info("client force close[]{}",client,e);
            delClient(client);
            log.info("client count is :{}",clients.size());
            key.cancel();
            key.channel().close();
        }
    }

    /**
     * 处理写事件
     * @param key SelectionKey
     */
    private void handlerWriter(SelectionKey key)throws IOException{
        NIOClient client = null;
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            client = (NIOClient) key.attachment();
            if (client.hasData()) {
                ByteBuffer buffer = client.getBuffer();
                socketChannel.write(buffer);
                log.info("{} send data",client);
                //清空数据
                client.clear();
            }
        }catch (IOException e){
            log.error("send data error[]{}",client,e);
            delClient(client);
            log.info("client count is :{}",clients.size());
            key.cancel();
            key.channel().close();
        }
    }

    /**
     * 转发消息
     * @param client 需要转发的client
     */
    private void forwardMessage(NIOClient client){
        if(client == null){
            return;
        }
        for(NIOClient c:clients){
            if(Arrays.equals(c.getOrigin(),client.getTarget())){
                c.setSendData(client.getForwardData());
                log.info("{} will send to {}",client,c);
                return;
            }
        }
        log.info("not find client[]{}",client);
    }


    /**
     * 添加设备
     * @param client 设备
     */
    private void addClient(NIOClient client) {
        if(client == null){
            return;
        }
        for(NIOClient c : clients){
            if(Arrays.equals(c.getOrigin(),client.getOrigin())){
                log.info("client already exist:{}",client);
                return;
            }
        }
        clients.add(client);
        log.info("add a client {}",client);
    }

    /**
     * 删除设备
     * @param client 要删除的设备
     */
    private void delClient(NIOClient client){
        if(client == null){
            return;
        }
        Iterator<NIOClient> it = clients.iterator();
        while(it.hasNext()){
            NIOClient c = it.next();
            if(Arrays.equals(c.getOrigin(),client.getOrigin())){
                it.remove();
                log.info("remove a client {}",client);
                return;
            }
        }
        log.info("not find client []{}",client);
    }

}










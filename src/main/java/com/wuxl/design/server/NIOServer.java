package com.wuxl.design.server;

import com.wuxl.design.client.NIOClient;
import com.wuxl.design.client.impl.NIOClientImpl;
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

/**
 * 服务器
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOServer {

    private static final Logger log = LoggerFactory.getLogger(NIOServer.class);

    private static NIOServer instance;

    //客户端列表
    private List<NIOClient> clients = new ArrayList<>();

    private ByteBuffer buffer = ByteBuffer.allocate(32);

    private Selector selector;

    private boolean running;

    //服务绑定的端口号
    private int port;

    private NIOServer() {}

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
            log.info("服务器绑定在端口号:{}", port);
        } catch (IOException e) {
            log.error("服务器启动失败", e);
            throw e;
        }
        return instance;
    }

    /**
     * 开始运行
     */
    public void start() throws IOException {
        log.info("服务器开始运行...");
        running = true;
        instance.listen();
    }

    /**
     * 关闭服务
     */
    public void close() {
        log.info("服务器正在关闭...");
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
        try {
            while (running) {
                count = selector.select();
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
        }catch (Exception e) {
            log.error("服务调度出现异常", e);
        } finally {
            selector.close();
            log.info("服务器已关闭");
            running = false;
        }
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
        }catch (Exception e){
            log.error("事件处理发生异常",e);
        }
    }

    /**
     * 处理连接事件
     */
    private void handlerAccept(SelectionKey key){
        try {
            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector,
                    SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            NIOClient client = new NIOClientImpl();
            selectionKey.attach(client);
            SocketAddress address = socketChannel.getRemoteAddress();
            log.info("获得了新的客户端连接:{}", address);
        }catch (IOException e){
            log.error("客户端连接时出现异常",e);
        }
    }

     /**
     * 处理读事件
     */
    private void handlerReader(SelectionKey key)throws IOException{
        SocketChannel socketChannel = null;
        NIOClient client = null;
        try {
            socketChannel = (SocketChannel) key.channel();
            client = (NIOClient) key.attachment();
            DataPackage dataPackage = client.getDataPackage();
            //接收数据
            int count = socketChannel.read(buffer);
            if (count != -1) {
                log.info("收到{}发送的数据count={}", client,count);
                //buffer从写模式切换到读模式
                buffer.flip();
                byte[] arrays = buffer.array();
                log.info("原始数据为:{}",Arrays.toString(Arrays.copyOf(arrays,count)));
                boolean isSuc = dataPackage.receive(arrays);
                if(!isSuc){
                    log.info("数据异常,请求被忽略:{}", Arrays.toString(Arrays.copyOf(arrays, count)));
                    return;
                }
                log.info("数据处理后:{}",dataPackage);
                //进行数据处理
                if (client.process()) {
                    //设置发送信息
                    setMessage(client);
                } else {
                    //添加设备
                    addClient(client);
                    log.info("当前设备数为:{}",clients.size());
                }
                buffer.clear();
            } else {
                log.info("客户端已断开[]{}",client);
                key.cancel();
                socketChannel.close();
                //删除设备
                delClient(client);
            }
        }catch (IOException e){
            log.info("读数据异常,该连接断开[]{}",client,e);
            key.cancel();
            socketChannel.close();
            delClient(client);
        }
    }

    /**
     * 处理写事件
     * @param key SelectionKey
     */
    private void handlerWriter(SelectionKey key)throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();
        NIOClient client = (NIOClient) key.attachment();
        try {
            if (client.hasData()) {
                ByteBuffer buffer = ByteBuffer.wrap(client.getDataPackage().getSendData());
                socketChannel.write(buffer);
                //清空数据
                client.clear();
                log.info("设备发送了数据[]{}", client.getDataPackage());
            }
        }catch (IOException e){
            log.error("写数据异常,断开连接[]{}",client,e);
            key.cancel();
            socketChannel.close();
            delClient(client);
        }
    }

    /**
     * 添加设备
     * @param client 设备
     */
    private void addClient(NIOClient client) {
        for(NIOClient c : clients){
            if(Arrays.equals(c.getOrigin(),client.getOrigin())){
                log.info("该设备已存在:{}",client);
                return;
            }
        }
        clients.add(client);
        log.info("添加了一台设备{}",client);
    }

    /**
     * 设置发送信息
     */
    private void setMessage(NIOClient client) {
        DataPackage data = client.getDataPackage();
        for(NIOClient c: clients){
            if(Arrays.equals(c.getOrigin(),data.getTarget())){
                c.setDataPackage(data);
                c.setHasData();
                log.info("{}将发送到{}",client,c);
                return;
            }
        }
        log.info("数据包未找到目标设备[]{}", data);
    }

    /**
     * 删除设备
     * @param client 要删除的设备
     */
    private void delClient(NIOClient client){
        Iterator<NIOClient> it = clients.iterator();
        while(it.hasNext()){
            NIOClient c = it.next();
            if(Arrays.equals(c.getOrigin(),client.getOrigin())){
                it.remove();
                log.info("删除了设备{}",client);
                return;
            }
        }

        log.info("未找到要删除的设备[]{}",client);
    }

}










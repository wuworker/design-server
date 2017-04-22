package com.wuxl.design;

import com.wuxl.design.protocol.DataExecutor;
import com.wuxl.design.protocol.DataPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wuxingle on 2017/4/17 0017.
 * 与android手机通信测试
 */
public class AndroidTest {

    //命令
    //开启
    public static final byte CMD_ON = 0x12;
    //关闭
    public static final byte CMD_OFF = 0x49;
    //调整
    public static final byte CMD_PWM = (byte)0xa1;
    //是否在线
    public static final byte CMD_ONLINE = (byte)0xf5;

    //应答
    public static final byte OK_ONLINE = (byte)0xa8;

    private static final byte[] ID1 = new byte[6];
    private static final byte[] ANDROID = new byte[6];
    private static final byte[] EMPTY = new byte[6];

    static {
        Arrays.fill(ID1,(byte)0x2f);
        Arrays.fill(ANDROID,(byte)0x23);
    }

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        DataExecutor dataExecutor = DataExecutor.getClientDataEecutor();

        service.execute(new Device1(dataExecutor));

        service.shutdown();
    }

    /**
     * 模拟一个设备
     */
    private static class Device1 implements Runnable{

        private DataPackage dataPackage;

        private DataExecutor dataExecutor;

        private byte[] data = new byte[4];

        public Device1(DataExecutor dataExecutor){
            this.dataExecutor = dataExecutor;
            Arrays.fill(data,(byte)1);
            dataPackage = new DataPackage(ID1,EMPTY,data);
        }

        @Override
        public void run() {
            try (Socket socket = new Socket("localhost",9999)){
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                byte[] sendData = dataExecutor.formDataPackage(dataPackage);
                out.write(sendData);
                out.flush();
                System.out.println("发送的数据为:"+Arrays.toString(sendData));
                int count = 0;
                byte[] receiveData = new byte[20];
                while((count=in.read(receiveData))!=-1){
                    DataPackage dataPackage = dataExecutor.toDataPackage(receiveData);
                    System.out.println("接收到:"+dataPackage);
                    //设置应答
                    System.arraycopy(dataPackage.getOrigin(),0,dataPackage.getTarget(),0,dataPackage.getTargetLength());
                    System.arraycopy(ID1,0,dataPackage.getOrigin(),0,dataPackage.getOriginLength());
                    byte[] data = dataPackage.getData();
                    data[0] = OK_ONLINE;
                    sendData = dataExecutor.formDataPackage(dataPackage);
                    out.write(sendData);
                    out.flush();
                    System.out.println("发送了:"+Arrays.toString(sendData));
                }
                System.out.println("与服务器断开连接");

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                System.out.println("结束");
            }
        }
    }





}


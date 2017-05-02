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

import static com.wuxl.design.protocol.DataProtocol.*;

/**
 * Created by wuxingle on 2017/4/17 0017.
 * 与android手机通信测试
 */
public class AndroidTest {

    //手机
    private static final byte[] ANDROID = new byte[6];

    //单片机1
    private static final byte[] MAC1 = {94,207-256,127,241-256,220-256,96};

    static {
        Arrays.fill(ANDROID,(byte)0x23);
    }

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        DataExecutor dataExecutor = DataExecutor.getDefaultDataExecutor();

        service.execute(new Device1(dataExecutor));

        service.shutdown();
    }

    /**
     * 模拟一个设备
     */
    private static class Device1 implements Runnable{

        private DataPackage dataPackage;

        private DataExecutor dataExecutor;

        public Device1(DataExecutor dataExecutor){
            this.dataExecutor = dataExecutor;
            dataPackage = new DataPackage();
            dataExecutor.setDataPackage(dataPackage);
            dataPackage.setOrigin(MAC1);
            dataPackage.setTarget(ANDROID);
            dataPackage.setCmd(IS_MCU);
        }

        @Override
        public void run() {
            try (Socket socket = new Socket("localhost",9999)){
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                byte[] sendData = dataExecutor.fromDataPackage(dataPackage);
                out.write(sendData);
                out.flush();
                System.out.println("发送的数据为:"+Arrays.toString(sendData));
                int count = 0;
                byte[] receiveData = new byte[30];
                while((count=in.read(receiveData))!=-1){
                    byte[] array = Arrays.copyOf(receiveData,count);
                    //System.out.println(Arrays.toString(array));
                    if(array.length < PACKET_MIN_LENGTH){
                        continue;
                    }
                    DataPackage dataPackage = dataExecutor.toDataPackage(array);
                    System.out.println(dataPackage == this.dataPackage);
                    System.out.println("接收到:"+dataPackage);
                    //设置应答
                    dataPackage.clear();
                    dataPackage.setOrigin(MAC1);
                    dataPackage.setTarget(ANDROID);
                    dataPackage.setCmd(OK);
                    sendData = dataExecutor.fromDataPackage(dataPackage);
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


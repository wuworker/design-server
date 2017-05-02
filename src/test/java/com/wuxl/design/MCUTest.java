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

import static com.wuxl.design.protocol.DataProtocol.IS_APP;
import static com.wuxl.design.protocol.DataProtocol.IS_MCU;

/**
 * Created by wuxingle on 2017/4/30 0030.
 * 与单片机的通信测试
 */
public class MCUTest {

    private static final byte[] ORIGIN1 = {0x21,0x21,0x21,0x21,0x21,0x21};

    private static final byte[] ORIGIN2 = {0x34,0x34,0x34,0x34,0x34,0x34};

    //单片机1
    private static final byte[] MAC1 = {94,207-256,127,241-256,220-256,96};

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        DataExecutor dataExecutor1 = DataExecutor.getDefaultDataExecutor();
        DataExecutor dataExecutor2 = DataExecutor.getDefaultDataExecutor();

        byte[] data1=new byte[]{1,2,3,4};
        byte[] data2 = new byte[]{100,101,102,103};

        service.execute(new Task1(dataExecutor1,data1));
        //service.execute(new Task2(dataExecutor2,data2));

        service.shutdown();
    }

    private static class Task1 implements Runnable {

        private DataPackage dataPackage;
        private DataExecutor dataExecutor;

        public Task1(DataExecutor dataExecutor, byte[] data) {
            this.dataPackage = new DataPackage();
            this.dataExecutor = dataExecutor;
            this.dataExecutor.setDataPackage(dataPackage);
            dataPackage.setOrigin(ORIGIN1);
            dataPackage.setCmd(IS_APP);
            dataPackage.setData(data);
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 9999);
                OutputStream out = socket.getOutputStream();
                //自我设置数据
                byte[] bytes = dataExecutor.fromDataPackage(dataPackage);
                out.write(bytes);
                out.flush();
                System.out.println("发送了:" + Arrays.toString(bytes));
                //发送到id2
                dataPackage.setTarget(MAC1);
                dataPackage.setCmd((byte)0x12);
                bytes = dataExecutor.fromDataPackage(dataPackage);
                int num = 0;
                while((num=System.in.read())!=-1){
                    //输入s关闭
                    if(num==115){
                        socket.shutdownOutput();
                        break;
                    }
                    out.write(bytes);
                    out.flush();
                    System.out.println("发送了:" + Arrays.toString(bytes));
                }
                socket.close();
                System.out.println("已关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Task2 implements Runnable {

        private DataPackage dataPackage;
        private DataExecutor dataExecutor;

        public Task2(DataExecutor dataExecutor, byte[] data) {
            this.dataPackage = new DataPackage();
            this.dataExecutor = dataExecutor;
            this.dataExecutor.setDataPackage(dataPackage);
            dataPackage.setOrigin(ORIGIN2);
            dataPackage.setCmd(IS_MCU);
            dataPackage.setData(data);
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 9999);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                //自我设置数据
                byte[] bytes = dataExecutor.fromDataPackage(dataPackage);
                out.write(bytes);
                out.flush();
                System.out.println("发送了:" + Arrays.toString(bytes));
                int count = 0;
                byte[] data = new byte[32];
                while ((count=in.read(data))!=-1){
                    System.out.println(Arrays.toString(Arrays.copyOf(data,count)));
                }
//                byte[] data = new byte[32];
//                for (int i = 0; i < 5 && (count = in.read(data)) > 0; i++) {
//                    byte[] array = Arrays.copyOf(data,count);
//                    DataPackage dataPackage = dataExecutor.toDataPackage(array);
//                    System.out.println("接收了:" + dataPackage);
//                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

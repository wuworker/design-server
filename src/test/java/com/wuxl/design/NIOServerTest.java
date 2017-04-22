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
 * Created by wuxingle on 2017/4/9 0009.
 * 通信测试
 */
public class NIOServerTest {

    private static final byte[] ID1 = new byte[6];
    private static final byte[] ID2 = new byte[6];
    private static final byte[] ID3 = new byte[6];
    private static final byte[] EMPTY = new byte[6];

    static {
        Arrays.fill(ID1, (byte) 0x1f);
        Arrays.fill(ID2, (byte) 0x23);
        Arrays.fill(ID3, (byte) 0x56);
    }

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(3);
        DataExecutor dataExecutor1 = DataExecutor.getClientDataEecutor();
        DataExecutor dataExecutor2 = DataExecutor.getClientDataEecutor();

        byte[] data1=new byte[]{1,2,3,4};
        byte[] data2 = new byte[]{100,101,102,103};

        service.execute(new Task1(dataExecutor1,data1));
        service.execute(new Task2(dataExecutor2,data2));

        service.shutdown();
    }


    private static class Task1 implements Runnable {

        private DataPackage dataPackage;
        private DataExecutor dataExecutor;

        public Task1(DataExecutor dataExecutor, byte[] data) {
            this.dataPackage = new DataPackage();
            this.dataExecutor = dataExecutor;
            System.arraycopy(ID1, 0, dataPackage.getOrigin(), 0, dataPackage.getOriginLength());
            System.arraycopy(data, 0, dataPackage.getData(), 0, dataPackage.getDataLength());
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 9999);
                OutputStream out = socket.getOutputStream();
                //自我设置数据
                byte[] bytes = dataExecutor.formDataPackage(dataPackage);
                out.write(bytes);
                out.flush();
                System.out.println("发送了:" + Arrays.toString(bytes));
                //发送到id2
                System.arraycopy(ID2,0,dataPackage.getTarget(),0,dataPackage.getTargetLength());
                Thread.sleep(1000);
                for (int i = 1; i < 6; i++) {
                    bytes = dataExecutor.formDataPackage(dataPackage);
                    out.write(bytes);
                    out.flush();
                    System.out.println("发送了:" + Arrays.toString(bytes));
                    Thread.sleep(1000);
                }
                socket.close();
            } catch (IOException | InterruptedException e) {
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
            System.arraycopy(ID2, 0, dataPackage.getOrigin(), 0, dataPackage.getOriginLength());
            System.arraycopy(data, 0, dataPackage.getData(), 0, dataPackage.getDataLength());
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 9999);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                //自我设置数据
                byte[] bytes = dataExecutor.formDataPackage(dataPackage);
                out.write(bytes);
                out.flush();
                System.out.println("发送了:" + Arrays.toString(bytes));
                int count = 0;
                byte[] data = new byte[16];
                for (int i = 0; i < 5 && (count = in.read(data)) > 0; i++) {
                    DataPackage dataPackage = dataExecutor.toDataPackage(data);
                    System.out.println("接收了:" + dataPackage);
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

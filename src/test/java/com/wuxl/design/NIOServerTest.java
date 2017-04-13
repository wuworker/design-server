package com.wuxl.design;

import com.wuxl.design.protocol.DataPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOServerTest {

    private static final byte[] ID1 = new byte[6];
    private static final byte[] ID2 = new byte[6];
    private static final byte[] ID3 = new byte[6];
    private static final byte[] EMPTY = new byte[6];

    public static void main(String[] args){
        ExecutorService service = Executors.newFixedThreadPool(5);

        initId();

        DataPackage data1 = new DataPackage(ID1,EMPTY,0x12345678);
        DataPackage data2 = new DataPackage(ID2,EMPTY,0x12458395);

        service.execute(new Task1(data1));
        //service.execute(new Task2(data2));

        service.shutdown();
    }

    private static void initId(){
        Arrays.fill(ID1,(byte)0x1f);
        Arrays.fill(ID2,(byte)0x23);
        Arrays.fill(ID3,(byte)0x56);
    }


    private static class Task1 implements Runnable{

        private DataPackage dataPackage;

        public Task1(DataPackage dataPackage){
            this.dataPackage = dataPackage;
        }

        @Override
        public void run() {
            try{
                Socket socket = new Socket("localhost",9999);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                byte[] bytes = dataPackage.getAllData();
                out.write(bytes);
                out.flush();
                System.out.println("发送了:"+Arrays.toString(bytes));
                dataPackage.setTarget(ID2);
                Thread.sleep(1000);
                for(int i=1;i<6;i++){
                    dataPackage.setData(dataPackage.getData() + 1);
                    bytes = dataPackage.getAllData();
                    out.write(bytes);
                    out.flush();
                    System.out.println("发送了:"+Arrays.toString(bytes));
                    Thread.sleep(1000);
                }
                socket.shutdownOutput();
                socket.close();
            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private static class Task2 implements Runnable{

        private DataPackage dataPackage;

        public Task2(DataPackage dataPackage){
            this.dataPackage = dataPackage;
        }

        @Override
        public void run() {
            try{
                Socket socket = new Socket("localhost",9999);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                out.write(dataPackage.getAllData());
                out.flush();
                int count = 0;
                byte[] data = new byte[16];
                for(int i=0;i<5 && (count = in.read(data))>0;i++){
                    System.out.println("接收了:"+Arrays.toString(data));
                }
                socket.shutdownOutput();
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}

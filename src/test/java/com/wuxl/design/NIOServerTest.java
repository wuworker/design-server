package com.wuxl.design;

import com.wuxl.design.protocol.DataPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.wuxl.design.protocol.DataProtocol.CMD_MY;
import static com.wuxl.design.protocol.DataProtocol.TYPE_APP;

/**
 *
 * Created by wuxingle on 2017/4/9 0009.
 */
public class NIOServerTest {

    private static final byte[] ID1 = new byte[16];
    private static final byte[] ID2 = new byte[16];
    private static final byte[] ID3 = new byte[16];
    private static final byte[] ID4 = new byte[16];

    public static void initId(){
        Arrays.fill(ID1,(byte)0x1f);
        Arrays.fill(ID2,(byte)0x23);
        Arrays.fill(ID3,(byte)0x56);
        Arrays.fill(ID4,(byte)0xab);
    }

    public static void main(String[] args){
        ExecutorService service = Executors.newFixedThreadPool(5);

        initId();

        DataPackage data1 = new DataPackage(ID1,TYPE_APP,CMD_MY,(byte)99);
        DataPackage data2 = new DataPackage(ID2,TYPE_APP,CMD_MY,(byte)10);

        service.execute(new Task2(data2));

        try {
            Thread.sleep(5000);
            service.execute(new Task1(data1));

        }catch (InterruptedException e){
            e.printStackTrace();
        }

        service.shutdown();
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
                out.write(dataPackage.getSendData());
                out.flush();
                dataPackage.setId(ID2);
                dataPackage.setCmd((byte)0x12);
                Thread.sleep(1000);
                for(int i=0;i<5;i++){
                    dataPackage.setData((byte)(100+i));
                    out.write(dataPackage.getSendData());
                    out.flush();
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
                out.write(dataPackage.getSendData());
                out.flush();
                int count = 0;
                byte[] data = new byte[32];
                for(int i=0;i<5 && (count = in.read(data))>0;i++){
                    System.out.println(Arrays.toString(data));
                }
                socket.shutdownOutput();
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}

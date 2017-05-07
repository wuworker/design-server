package com.wuxl.design.server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuxingle on 2017/5/5.
 * server的配置类
 */
public class NIOServerConfiguration {

    //参数
    private Map<NIOServerOption,String> options = new HashMap<>();


    public NIOServerConfiguration()throws DocumentException{
        this(null);
    }

    public NIOServerConfiguration(String configFile)throws DocumentException{
        //配置默认xml文件
        doXMLConfig(getXMLDocument(getClass().getResourceAsStream("/design-server.xml")));

        if(configFile != null){
            File newFile = new File(configFile);
            if(newFile.exists()){
                doXMLConfig(getXMLDocument(newFile));
            }
        }
    }

    /**
     * 获取参数配置
     */
    public Map<NIOServerOption,String> getOptions(){
        return options;
    }

    /**
     * 读取xml
     */
    private Document getXMLDocument(InputStream in)throws DocumentException{
        SAXReader reader = new SAXReader();
        return reader.read(in);
    }

    private Document getXMLDocument(File file)throws DocumentException{
        SAXReader reader = new SAXReader();
        return reader.read(file);
    }

    /**
     * 解析xml进行配置
     */
    @SuppressWarnings("unchecked")
    private void doXMLConfig(Document document)throws DocumentException{
        Element root = document.getRootElement();
        if(root == null || !root.getName().equals("design-server")){
            return;
        }
        Element serverNode = root.element("server");
        if(serverNode!=null){
            Element portNode = serverNode.element("port");
            Element heartNode = serverNode.element("heart");
            setXmlOptions(portNode);
            if(heartNode!=null){
                Element start = heartNode.element("start");
                Element period = heartNode.element("period");
                setXmlOptions(start);
                setXmlOptions(period);
            }
        }
    }

    /**
     * 设置xml参数
     */
    private void setXmlOptions(Element element){
        if(element == null){
            return;
        }
        NIOServerOption option = NIOServerOption.getByString(element.getName());
        options.put(option,element.getStringValue());
    }

}

package org.zhw.mvc.springmvc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * xml 解释器
 * @Author zhw
 * @since 2022/1/17
 */
public class XmlPaser {

    /**
     * 获取xml 文件 component-scan 的属性base-package 值
     * @param xml
     * @return
     */
    public static String getBasePackage(String xml){
        try {
            SAXReader saxReader = new SAXReader();
            InputStream inputStream = XmlPaser.class.getClassLoader().getResourceAsStream(xml);
            //xml 文档
            Document document = saxReader.read(inputStream);
            Element rootElement = document.getRootElement();
            Element componentScan =  rootElement.element("component-scan");
            Attribute attribute = componentScan.attribute("base-package");
            String basePackage = attribute.getText();
            return basePackage;
        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }
        return "";
    }
}

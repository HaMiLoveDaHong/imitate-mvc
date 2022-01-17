package org.zhw.mvc.springmvc.context;

import org.zhw.mvc.springmvc.annotation.AutoWired;
import org.zhw.mvc.springmvc.annotation.Controller;
import org.zhw.mvc.springmvc.annotation.Service;
import org.zhw.mvc.springmvc.xml.XmlPaser;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zhw
 * @since 2022/1/17
 */
public class WebApplicationContext {

    //classpath:springmvc.xml
    private String configurations;
    //定义集合  用于存放 bean 的权限名|包名.类名
    public ArrayList<String> classNamesList=new ArrayList<String>();
    //定义web容器，key:类名称 首字母小写，value: 实例对象
    public Map<String,Object> webMap = new ConcurrentHashMap<String, Object>();

    public WebApplicationContext() {
    }

    public WebApplicationContext(String configurations) {
        this.configurations = configurations;
    }


    /**
     * 初始化 web容器
     */
    public void onRefresh(){

        //第一步操作：加载 classpath:springmvc.xml base-package=org.zhw.mvc.controller,org.zhw.mvc.service
        String basePackage = XmlPaser.getBasePackage(configurations.split(":")[1]);
        
        //第二步操作：进行 包扫描
        String[] basePackages = basePackage.split(",");
        for (String aPackage : basePackages) {
            excuteScanPackage(aPackage);
        }

        //第三步操作：通过反射->实例化容器中bean
        excuteInstance();

        //第四步操作：通过自动注解注入依赖
        excuteAutoWired();
    }

    /**
     * 通过自动注解注入依赖
     */
    public void excuteAutoWired() {
        try {
            for (Map.Entry<String, Object> objectEntry : webMap.entrySet()) {
                Object o = objectEntry.getValue();
                Field[] fields = o.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(AutoWired.class)){
                        AutoWired autoWired = field.getAnnotation(AutoWired.class);
                        String beanName = autoWired.value();
                        //取消检查机制
                        field.setAccessible(true);
                        field.set(o,webMap.get(beanName));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 通过反射->实例化容器中bean
     */
    public void excuteInstance() {

        for (String className:classNamesList){
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)){
                    Object instance = clazz.newInstance();
                    String name = clazz.getSimpleName().substring(0,1).toLowerCase()+ clazz.getSimpleName().substring(1);
                    //保存到web容器中
                    webMap.put(name,instance);
                }else if (clazz.isAnnotationPresent(Service.class)){
                    Service annotation = clazz.getAnnotation(Service.class);
                    Object instance = clazz.newInstance();
                    String name = annotation.value();
                    //保存到web容器中
                    webMap.put(name,instance);
                }
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 扫描 包下的类名称与路径
     * @param basePackage
     */
    public void excuteScanPackage(String basePackage){
        //把包路径转换成文件路径
        String path = basePackage.replaceAll("\\.","/");
        URL url = this.getClass().getClassLoader().getResource("/" + path);
        //文件路径
        String file = url.getFile();

        //文件
        File dir = new File(file);

        for (File f : dir.listFiles()) {
            if (f.isDirectory()){
                //当前是一个文件目录  org.zhw.mvc.service.impl
                excuteScanPackage(basePackage+"."+f.getName());
            }else {
                //拼接 org.zhw.mvc.controller.UserController
                String className =basePackage+"."+f.getName().replaceAll("\\.class","") ;
                classNamesList.add(className);
            }
        }

    }
}

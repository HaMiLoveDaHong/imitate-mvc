package org.zhw.mvc.springmvc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zhw.mvc.springmvc.annotation.Controller;
import org.zhw.mvc.springmvc.annotation.RequestMapping;
import org.zhw.mvc.springmvc.annotation.ResponseBody;
import org.zhw.mvc.springmvc.context.WebApplicationContext;
import org.zhw.mvc.springmvc.handler.MyHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author zhw
 * @since 2022/1/17
 */
public class DispatcherServlet extends HttpServlet {

    //指定SpringMvc容器
    private WebApplicationContext webApplicationContext;

    //创建集合  用于存放  映射关系    映射地址  与  控制器方法，用于发送请求直接从该集合中进行匹配
    public ArrayList<MyHandler> handlerList = new ArrayList<MyHandler>();

    @Override
    public void init() throws ServletException {

        //1、加载初始化参数   classpath:springmvc.xml
        String configurations = this.getServletConfig().getInitParameter("contextConfigLocaltion");

        //2、创建 web 容器
        webApplicationContext = new WebApplicationContext(configurations);

        //3.进行初始化操作
        webApplicationContext.onRefresh();

        //4.初始化请求映射关系   /findUser   ===》控制器.方法
        initHandleMapping();
    }

    /**
     * 初始化请求映射关系 只要是controller 含有RequestMapping方法
     */
    public void initHandleMapping(){
        try{
            for (Map.Entry<String, Object> objectEntry : webApplicationContext.webMap.entrySet()) {
                Object o = objectEntry.getValue();
                if (o.getClass().isAnnotationPresent(Controller.class)){
                    Method[] methods = o.getClass().getMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(RequestMapping.class)){
                            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                            String url = requestMapping.value();
                            MyHandler myHandler = new MyHandler(url,o,method);
                            handlerList.add(myHandler);
                        }
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //进行请求分发处理
        doDispatcherServlet(req,resp);

    }


    public void doDispatcherServlet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.setCharacterEncoding("utf-8");
            //获取处理器对象
            MyHandler myHandler = getMyHandler(req);

            if (myHandler==null){
                PrintWriter respWriter = resp.getWriter();
                respWriter.print("<h1>404 NOT  FOUND!</h1>");
                respWriter.flush();
                respWriter.close();
            }else {
                //调用处理方法之前 进行参数的注入
                Method method = myHandler.getMethod();
                //方法参数名称
                Parameter[] parameters = method.getParameters();
                List<Object> strings = new ArrayList<>();
                for (Parameter parameter : parameters) {
                    Object o1 = parameter.getType().newInstance();
                    if (o1 instanceof  String){
                        String o = req.getParameter(parameter.getName());
                        strings.add(o);
                    }else if (o1 instanceof Object){
                        Class<?> aClass = o1.getClass();
                        Field[] fields = aClass.getDeclaredFields();
                        for (Field field : fields) {
                            String reName = parameter.getName()+"."+field.getName();
                            Object o = req.getParameter(reName);
                            if (o != null){
                                field.setAccessible(true);
                                Class<?> type = field.getType();
                                String typeName = type.getTypeName();
                                if (typeName.contains("String") ){
                                    field.set(o1,new String(o.toString()));
                                }else if (typeName.contains("Integer")){
                                    field.set(o1,new Integer(o.toString()));
                                }
                            }
                        }
                        strings.add(o1);
                    }
                }

                //调用目标方法
                Object invoke = myHandler.getMethod().invoke(myHandler.getController(),strings.toArray());

                if (invoke instanceof String){
                    //转String page 页面
                    String pageName = String.valueOf(invoke);
                    if (pageName.contains(":")){
                        String viewType = pageName.split(":")[0];
                        String viewPage = pageName.split(":")[1];
                        if (viewType.equals("forward")){
                            req.getRequestDispatcher(pageName).forward(req,resp);
                        }else {
                            // redirect:/success.jsp
                            resp.sendRedirect(viewPage);
                        }
                    }else {
                        //默认转发
                        req.getRequestDispatcher(pageName).forward(req,resp);
                    }

                }else {
                    if (myHandler.getMethod().isAnnotationPresent(ResponseBody.class)){
                        ObjectMapper objectMapper = new ObjectMapper();
                        String value = objectMapper.writeValueAsString(invoke);
                        resp.setContentType("text/html;charset=utf-8");
                        PrintWriter respWriter = resp.getWriter();
                        respWriter.print(value);
                        respWriter.flush();
                        respWriter.close();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    public MyHandler getMyHandler(HttpServletRequest req){
        //获取请求的路径对象
        String requestURI = req.getRequestURI();
        for (MyHandler handler : handlerList) {
            if (handler.getUrl().equals(requestURI)){
                return handler;
            }
        }
        return  null;
    }
}

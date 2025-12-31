package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
public class WebSocketConfiguration {


//    WebSocketConfiguration 类中的 ServerEndpointExporter Bean 的主要作用是 自动注册使用了 @ServerEndpoint 注解的 Bean。
//
//    工作原理：当 Spring 容器启动时，ServerEndpointExporter 会去扫描 Spring 容器中所有被 @ServerEndpoint 注解的类（比如你的 WebSocketServer），
//    并把它们注册到底层的 WebSocket 容器（比如 Spring Boot 内置的 Tomcat）中。
//
//    通俗理解：你的 WebSocketServer 类虽然写好了代码，贴好了标签（注解），但如果没有这个“扫描仪”去通知 Tomcat，
//    Tomcat 根本不知道这里有一个 WebSocket 服务，也不会把网络请求转发给它。
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


}

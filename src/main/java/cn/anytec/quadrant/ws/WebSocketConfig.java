package cn.anytec.quadrant.ws;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements

        WebSocketConfigurer {

//    public WebSocketConfig() {
//
//    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //================视频回显================
        // 用来注册websocket server实现类，第二个参数是访问websocket的地址
        registry.addHandler(systemWebSocketHandler(), "/webSocketServer");
        // 使用Sockjs的注册方法
        registry.addHandler(systemWebSocketHandler(), "/sockjs/webSocketServer").addInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
                return false;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

            }
        }).withSockJS();
        //================识别推送=================

        registry.addHandler(ffWebsocketHandler(), "ffWebSocket");
        // 使用Sockjs的注册方法
        registry.addHandler(ffWebsocketHandler(), "/sockjs/ffWebSocket").addInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
                return false;
            }

            @Override
            public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

            }
        }).withSockJS();
    }

//    @Bean
    public WebSocketHandler systemWebSocketHandler() {
        return new SystemWebSocketHandler();
    }
    public WebSocketHandler ffWebsocketHandler(){
        return new FfWebsocketHandler();
    }

}
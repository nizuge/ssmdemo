package cn.anytec.quadrant.ws;

import cn.anytec.quadrant.util.WebSocketCommand;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

@Component
public class FfWebsocketHandler implements WebSocketHandler{
    private static final Map<String,WebSocketSession> sessions = new HashMap<String,WebSocketSession>();
    private static final Logger logger = Logger.getLogger(FfWebsocketHandler.class);

    public static WebSocketSession getWebSocketSession(String wssId){

        return sessions.get(wssId);
    }

//    static {
//        try {
//            ss = new ServerSocket(8100);
//            ss.accept();
////            Socket socket = ss.
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //    private static final IPCServer ipc = new IPCServer(8100);
//    static {
//        ipc.Start(null);
//    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            logger.debug("用户："+session.getId()+"连接Server成功");
            sessions.put(session.getId(),session);

            //session.sendMessage(new TextMessage("服务端链接成功！"));
    }

    @Override
    public void handleMessage(WebSocketSession wss, WebSocketMessage<?> wsm) throws Exception {
        String cmd = wsm.getPayload().toString();
        WebSocketCommand.ffParseCommand(cmd,wss);
    }

    @Override
    public void handleTransportError(WebSocketSession wss, Throwable thrwbl) throws Exception {
        if(wss.isOpen()){
            wss.close();
        }
        sessions.remove(wss.getId());
        DataPushScheduler.removeWssId(wss.getId());
        logger.debug("ERROR! 用户："+wss.getId()+"从Server断开");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus cs) throws Exception {
        sessions.remove(wss.getId());
        DataPushScheduler.removeWssId(wss.getId());
        logger.debug("用户："+wss.getId()+"从Server断开");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

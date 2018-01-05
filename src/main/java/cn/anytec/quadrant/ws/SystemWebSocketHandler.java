package cn.anytec.quadrant.ws;


import cn.anytec.quadrant.util.WebSocketCommand;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

@Component
public class SystemWebSocketHandler implements WebSocketHandler {
    private static final Map<String,WebSocketSession> sessions = new HashMap<String,WebSocketSession>();
    private static ServerSocket ss = null;
    private static final Logger logger = Logger.getLogger(SystemWebSocketHandler.class);

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
        try{
            logger.debug("用户："+session.getId()+"连接Server成功");
            sessions.put(session.getId(),session);
            WsMessStore.getInstance().addSession(session);
                //session.sendMessage(new TextMessage("服务端链接成功！"));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void handleMessage(WebSocketSession wss, WebSocketMessage<?> wsm) throws Exception {
        String cmd = wsm.getPayload().toString();
        WebSocketCommand.videoParseCommand(cmd,wss);
    }

    @Override
    public void handleTransportError(WebSocketSession wss, Throwable thrwbl) throws Exception {
        if(wss.isOpen()){
            wss.close();
        }
        sessions.remove(wss.getId());
        WsMessStore.getInstance().removeSession(wss);
        WsMessStore.removeWssId(wss.getId());
//        System.out.println("WebSocket出错！");
        logger.debug("ERROR! 用户："+wss.getId()+"从Server断开");



    }

    @Override
    public void afterConnectionClosed(WebSocketSession wss, CloseStatus cs) throws Exception {
        sessions.remove(wss.getId());
        WsMessStore.getInstance().removeSession(wss);
        WsMessStore.removeWssId(wss.getId());
        logger.debug("用户："+wss.getId()+"从Server断开");


    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
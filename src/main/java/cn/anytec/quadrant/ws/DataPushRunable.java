package cn.anytec.quadrant.ws;

import cn.anytec.quadrant.findface.IdentifyFace;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DataPushRunable implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(DataPushRunable.class);
    private List<Object> message = new ArrayList<Object>();
    private int invalidTimes = 0;
    private String mac;
    private String[] wssIds;

    public DataPushRunable(String mac){
        this.mac = mac;
    }

    @Override
    public void run() {

        Condition condition = DataPushScheduler.getCondition(mac);
        Lock lock = DataPushScheduler.getLock(mac);

        while(true){
            try {

                lock.lock();
                if(message.size() == 0){
                    logger.debug(mac+":推送线程等待");
                    condition.await();
                    logger.debug(mac+"推送线程唤醒");
                }
                lock.unlock();
                if(wssIds == null || wssIds.length == 0){
                    message.remove(0);
                    continue;
                }
                Object object = message.get(0);
                if(object instanceof byte[]){
                    byte[] pic = (byte[]) object;
                    logger.info("二进制数据长度："+pic.length);
                    String data = Base64.getEncoder().encodeToString(pic);

                    for(int i=0;i<wssIds.length;i++){
                        if(wssIds[i].equals("-1"))
                            continue;
                        WebSocketSession webSocketSession = FfWebsocketHandler.getWebSocketSession(wssIds[i]);
                        webSocketSession.sendMessage(new TextMessage(data));
                    }
                    message.remove(0);

                }else if(object instanceof IdentifyFace){
                    IdentifyFace face = (IdentifyFace)object;
                    byte[] faceNormalizedPhoto = face.getNormalizedPhoto();
                    if(faceNormalizedPhoto==null)
                        return;
                    String data = Base64.getEncoder().encodeToString(faceNormalizedPhoto);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("desc","identify_response");
                    jsonObject.put("pic",data);
                    jsonObject.put("meta",face.getMeta());
                    jsonObject.put("confidence",face.getConfidence());
                    jsonObject.put("galleries",face.getGalleries());
                    logger.info("推送一条已识别人脸数据");
                    for(int i=0;i<wssIds.length;i++){
                        if(wssIds[i].equals("-1"))
                            continue;
                        WebSocketSession webSocketSession = FfWebsocketHandler.getWebSocketSession(wssIds[i]);
                        webSocketSession.sendMessage(new TextMessage(jsonObject.toJSONString()));
                    }
                    message.remove(0);
                }else if(object instanceof String ){
                    String data = (String)object;
                    logger.info("String长度："+data.length());
                    logger.info(data);
                    message.remove(0);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void setIdentifyFace(IdentifyFace face,String camMac){
        wssIds = DataPushScheduler.getWssIds(camMac);
        message.add(face);
    }



}

package cn.anytec.quadrant.ws;

import cn.anytec.quadrant.CameraData.FDCameraData;
import cn.anytec.quadrant.findface.IdentifyFace;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

public class WsMessStore {
    private static final Logger logger = Logger.getLogger(WsMessStore.class);

    private static volatile Map<String,WebSocketSession> sessionsMap = new HashMap<>();
    private static List<Object> message = new ArrayList<Object>();
    private Thread pushMessageThread;
    private static Map<String,String[]> camMac_Wss_video_map = new HashMap<>();

    private static WsMessStore instance;
    public static WsMessStore getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (WsMessStore.class) {
                if (instance == null)
                    instance = new WsMessStore();
            }
        }
        return instance;
    }




    public void addSession(WebSocketSession session){

        sessionsMap.put(session.getId(),session);
    }

    public WebSocketSession getSession(String sessionId){

        return sessionsMap.get(sessionId);
    }
    public void removeSession(WebSocketSession session){
        logger.debug("removeWebSocketSession");
        sessionsMap.remove(session.getId());
    }
    public void addMessage(Object data){

        synchronized (this){
            message.add(data);
            logger.debug("添加一条数据成功，唤醒推送线程");
            this.notifyAll();
        }

    }
    public void startPushMessThread(){
        if(null!=pushMessageThread&&pushMessageThread.isAlive()){
            logger.debug("推送线程活跃中。。。");
            return;
        }
        pushMessageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    pushMessage();
                }
            }
        });

        pushMessageThread.start();

        logger.debug("推送线程启动，开始推送数据");
    }

    public void pushMessage(){

        try {
            synchronized (this) {
                while (message.size() == 0) {
                    try {
                        logger.debug("没有数据推送，等待中。。。");
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Object object = message.get(0);
                if(object instanceof FDCameraData){
                    FDCameraData fdCameraData = (FDCameraData) object;
                    logger.debug("二进制数据长度："+fdCameraData.mJpgData.length);
                    String data = Base64.getEncoder().encodeToString(fdCameraData.mJpgData);
                    String[] wssIds = camMac_Wss_video_map.get(fdCameraData.mStrMac);
                    if(wssIds == null || wssIds.length == 0)
                        return;
                    for (String wssId:wssIds){
                        if(wssId.equals("-1"))
                            continue;
                        WebSocketSession webSocketSession = sessionsMap.get(wssId);
                        synchronized (webSocketSession){
                            webSocketSession.sendMessage(new TextMessage(data));
                        }
                    }
                    message.remove(0);

                }/*else if(object instanceof IdentifyFace){
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

                    String[] wssIds = camMac_Wss_video_map.get(playingCamMac);
                    for (String wssId:wssIds){
                        sessionsMap.get(wssId).sendMessage(new TextMessage(jsonObject.toJSONString()));
                    }



                }else if(object instanceof String ){
                    String data = (String)object;
                    logger.debug("String长度："+data.length());

                    String[] wssIds = camMac_Wss_video_map.get(playingCamMac);
                    for (String wssId:wssIds){
                        sessionsMap.get(wssId).sendMessage(new TextMessage(data));
                    }
                    message.remove(0);
                }*/

            }
        }catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setWssId(String camMac,String[] wssId){
        camMac_Wss_video_map.put(camMac,wssId);
    }
    public static String[] getWssIds(String camMac){
        if(camMac_Wss_video_map.containsKey(camMac))
            return camMac_Wss_video_map.get(camMac);
        return null;
    }
    public static void removeWssId(String wssId){
        if(wssId == null || camMac_Wss_video_map.size() == 0)
            return;
        Iterator<String[]> iterator = camMac_Wss_video_map.values().iterator();
        while(iterator.hasNext()){
            String[] wssids = iterator.next();
            for (int i = 0;i<wssids.length;i++){
                if (wssids[i] == wssId)
                    wssids[i] = "-1";
            }
        }
    }


}

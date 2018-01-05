package cn.anytec.quadrant.util;

import cn.anytec.quadrant.ws.DataPushScheduler;
import cn.anytec.quadrant.ws.WsMessStore;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketCommand {

    public static void videoParseCommand(String cmd, WebSocketSession webSocketSession){

        try {


            WsMessStore.removeWssId(webSocketSession.getId());
            String key = cmd.split(":")[0];
            String info = cmd.substring(key.length() + 1);
            if (key.equals("mac")) {
                String[] wssIds = WsMessStore.getWssIds(info);
                if (wssIds == null) {
                    WsMessStore.setWssId(info, new String[]{webSocketSession.getId()});
                    return;
                }
                int length = 0;
                for (String wssid : wssIds) {
                    if (!wssid.equals("-1"))
                        length++;
                    if (wssid == webSocketSession.getId())
                        return;
                }
                String[] newWssIds = new String[length + 1];
                for (int i = 0; i < wssIds.length; i++) {
                    if (!wssIds[i].equals("-1"))
                        newWssIds[i] = wssIds[i];
                }
                newWssIds[length] = webSocketSession.getId();
                WsMessStore.setWssId(info, newWssIds);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void ffParseCommand(String cmd, WebSocketSession webSocketSession){

        DataPushScheduler.removeWssId(webSocketSession.getId());
        String key = cmd.split(":")[0];
        String info = cmd.substring(key.length()+1);
        if(key.equals("mac")){
            String[] wssIds = DataPushScheduler.getWssIds(info);
            if(wssIds == null) {
                DataPushScheduler.setWssId(info, new String[]{webSocketSession.getId()});
                return;
            }
            int length = 0;
            for(String wssid : wssIds){
                if(!wssid.equals("-1"))
                    length++;
                if(wssid == webSocketSession.getId())
                    return;
            }

            String[] newWssIds = new String[length+1];
            for (int i=0;i<wssIds.length;i++){
                if(!wssIds[i].equals("-1"))
                    newWssIds[i] = wssIds[i];
            }
            newWssIds[length] = webSocketSession.getId();
            DataPushScheduler.setWssId(info,newWssIds);
        }

    }
}

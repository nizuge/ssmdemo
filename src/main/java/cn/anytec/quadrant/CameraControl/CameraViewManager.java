package cn.anytec.quadrant.CameraControl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CameraViewManager {
    private static Map<String , CameraLiveViewer> viewers = new ConcurrentHashMap<>();
    private static CameraViewManager singleton = new CameraViewManager();
    private CameraViewManager () {}
    public static CameraViewManager getInstance() {
        return singleton;
    }
    public static CameraLiveViewer getCameraLiveViewer(String ip){
        return viewers.get(ip);
    }
    public static void setCameraLiveViewer(String ip,CameraLiveViewer cameraLiveViewer){
        viewers.put(ip,cameraLiveViewer);
    }
    public static void removeCameraLiveViewer(String ip){
        viewers.remove(ip);
    }
    public static boolean isKnownCamera(String ip){
        if(viewers.containsKey(ip)){
            return true;
        }
        return false;
    }
    public static Set<String> getCameraIPList(){
        return viewers.keySet();
    }
    public static Set<String> getLiveCameraIPList(){
        Set<String> set = new HashSet<>();
        Iterator<Map.Entry<String,CameraLiveViewer>> iterator= viewers.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,CameraLiveViewer> entry = iterator.next();
            if(entry.getValue().isOnline())
                set.add(entry.getKey());
        }
        return set;
    }
    public static String isoldMac(String mac){
        Iterator<Map.Entry<String,CameraLiveViewer>> iterator= viewers.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,CameraLiveViewer> entry = iterator.next();
            if(entry.getValue().getCamMac().equals(mac)){
                return entry.getKey();
            }
        }
        return null;
    }
}

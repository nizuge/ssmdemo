package cn.anytec.quadrant.util;

import cn.anytec.quadrant.findface.Constant;

import java.util.HashMap;
import java.util.Map;

public class CameraViewCount {

    private static Map<String,CameraViewCount> cameraViewCountMap = new HashMap<>(Constant.CAMERA_AMOUNT);

    public int times;
    public CameraViewCount(){
        this.times = 0;
    }
    public void count(){
        this.times++;
    }
    public static CameraViewCount getCameraViewCount(String mac){
        if(cameraViewCountMap.get(mac) == null){
            CameraViewCount cameraViewCount = new CameraViewCount();
            cameraViewCountMap.put(mac,cameraViewCount);
            return cameraViewCount;
        }
        return cameraViewCountMap.get(mac);
    }
}

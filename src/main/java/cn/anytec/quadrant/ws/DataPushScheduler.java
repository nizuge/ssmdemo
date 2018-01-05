package cn.anytec.quadrant.ws;

import cn.anytec.quadrant.findface.Constant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataPushScheduler {
    private static Map<String,Lock> lockMap = new HashMap<>(Constant.CAMERA_AMOUNT);
    private static Map<String,Condition> conditionMap = new HashMap<>();
    private static Map<String,String[]> camMac_Wss_ff_map = new HashMap<>();


    public static void createCondition(String camMac){
        if(camMac == null || camMac.equals("") || lockMap.containsKey(camMac))
            return;
        Lock lock = new ReentrantLock();
        lockMap.put(camMac,lock);
        Condition condition = lock.newCondition();
        conditionMap.put(camMac,condition);
    }
    public static Condition getCondition(String camMac){
        if(!conditionMap.containsKey(camMac))
            return null;
        return conditionMap.get(camMac);
    }
    public static Lock getLock(String camMac){
        if(!lockMap.containsKey(camMac))
            createCondition(camMac);
        return lockMap.get(camMac);
    }


    public static void setWssId(String camMac,String[] wssId){
        camMac_Wss_ff_map.put(camMac,wssId);
    }
    public static String[] getWssIds(String camMac){
        if(camMac_Wss_ff_map.containsKey(camMac))
            return camMac_Wss_ff_map.get(camMac);
        return null;
    }
    public static void removeWssId(String wssId){
        if(wssId == null || camMac_Wss_ff_map.size() == 0)
            return;
        Iterator<String[]> iterator = camMac_Wss_ff_map.values().iterator();
        while(iterator.hasNext()){
            String[] wssids = iterator.next();
            for (int i=0;i<wssids.length;i++){
                if (wssids[i] == wssId)
                    wssids[i] = "-1";
            }
        }
    }
}

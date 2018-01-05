package cn.anytec.quadrant.findface;

import cn.anytec.quadrant.CameraData.FDCameraData;
import cn.anytec.quadrant.CameraData.FaceDefine;
import cn.anytec.quadrant.util.FindFaceUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FindFaceHandler {
    private static Logger logger = LoggerFactory.getLogger(FindFaceHandler.class);
   // private static ArrayDeque<FDCameraData> fdCameraDataArrayDeque = new ArrayDeque<>(8);
    private static Long timeflag = System.currentTimeMillis();
    private static ExecutorService cameraFindfaceThreadPool;
    private static Map<String,Boolean> cameraThreadMap = new HashMap<>(Constant.CAMERA_AMOUNT);
    private Thread thread;
    private static FindFaceHandler singleton = new FindFaceHandler();
    private static JSONParser jsonParser = new JSONParser();
    private static int count;


    static{
        cameraFindfaceThreadPool = Executors.newFixedThreadPool(Constant.CAMERA_AMOUNT);
    }

    protected static void setCameraThreadStatus(String mac,boolean status){
        cameraThreadMap.put(mac,status);
    }
    private FindFaceHandler(){}
    public static FindFaceHandler getInstance(){
        return singleton;
    }
    public void notifyFindFace(FDCameraData data){

        logger.info("SIZE:"+cameraThreadMap.size());

        if((!cameraThreadMap.containsKey(data.mStrMac))||(!cameraThreadMap.get(data.mStrMac))){
            logger.info("有此Mac:"+cameraThreadMap.containsKey(data.mStrMac));
            if(cameraThreadMap.containsKey(data.mStrMac))
                logger.info("此Mac线程还活着:"+cameraThreadMap.get(data.mStrMac));
            logger.info("创建一个新线程"+data.mJpgSize);
            Thread cameraThread = new Thread(new FindFaceRunable(data));
            cameraThread.setDaemon(true);
            cameraThreadMap.put(data.mStrMac,true);
            cameraFindfaceThreadPool.execute(cameraThread);
            return;
        }

       /* if(fdCameraDataArrayDeque.size()>8)
            fdCameraDataArrayDeque.poll();
        fdCameraDataArrayDeque.add(data);
        if(thread==null||!thread.isAlive()){
            logger.info("启用备用线程处理数据");
            thread = new Thread(new FindFaceRunable(fdCameraDataArrayDeque.poll()));
            thread.setDaemon(true);
            thread.start();
        }*/



    }


    public void requestAnytecSDK(FaceDefine faceDefine, FDCameraData fdCameraData, String[] metas){
        count++;
        int x1= Double.valueOf(faceDefine.left).intValue();
        int x2 = Double.valueOf(faceDefine.right).intValue();
        int y1 = Double.valueOf(faceDefine.top).intValue();
        int y2 = Double.valueOf(faceDefine.bottom).intValue();
        int width = x2-x1;
        int height = y2-y1;
        String path = "/home/anytec-z/Pictures/sdpImages/"+count+":["+x1+","+x2+","+y1+","+y2+"]";
        for(String s:metas){
            path=path+","+s;
        }

        ByteArrayInputStream in = null;
        try {

            in = new ByteArrayInputStream(fdCameraData.mJpgData);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
            ImageReader reader =  readers.next();
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(in);
            reader.setInput(imageInputStream, true);
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rect = new Rectangle(x1, y1, width, height);
            param.setSourceRegion(rect);
            BufferedImage imageHandle = reader.read(0, param);
            //=====画框用的==============================
           /* BufferedImage image = ImageIO.read(in);
            Graphics g = image.getGraphics();
            g.setColor(Color.RED);//画笔颜色

            g.drawRect(x1, y1, width, height);*/
            //g.dispose();
            //=====画框用的==============================
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            ImageIO.write(imageHandle, Constant.PIC_FORMAT, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in!=null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }
    public FaceDefine[] imageDetect(byte[] image){
        logger.info("===========detect============");
        HttpResponse response = null;
        int i = 0;
        FaceDefine[] faceDefines = null;
        try {
            response = Request.Post(Constant.SDK_IP+"/v0/detect")
                    .connectTimeout(10000)
                    .socketTimeout(30000)
                    .addHeader("Authorization", "Token " + Constant.TOKEN)
                    .body(MultipartEntityBuilder
                            .create()
                            //.addTextBody("mf_selector", "all")
                            .addBinaryBody("photo", image, ContentType.create("image/jpeg"), "photo.jpg")
                            .build())
                    .execute().returnResponse();
            String reply = EntityUtils.toString(response.getEntity());
            logger.info(reply);
            int responseCode = response.getStatusLine().getStatusCode();
            if(responseCode!=200){
                logger.info("请求未正确响应："+responseCode);
                logger.info(reply);
                return null;
            }
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reply);
            if(jsonObject.containsKey("faces")){
                JSONArray faceArray = (JSONArray) jsonObject.get("faces");
                int faceNum = faceArray.size();
                if(faceArray.size()==0){
                    logger.error("图片中没有人脸！");
                    return null;
                }
                faceDefines = new FaceDefine[faceNum];
                Iterator iterator1 = faceArray.iterator();
                while (iterator1.hasNext()){
                    JSONObject face = (JSONObject)(iterator1.next());
                    FaceDefine faceDefine = new FaceDefine();

                    faceDefine.left = (double)(long)face.get("x1");
                    faceDefine.right = (double)(long)face.get("x2");
                    faceDefine.top = (double)(long)face.get("y1");
                    faceDefine.bottom = (double)(long)face.get("y2");
                    faceDefines[i] = faceDefine;
                    i++;
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return faceDefines;
    }
    public IdentifyFace imageIdentify(FaceDefine faceDefine,FDCameraData data){
        logger.info("===========identify============");
        HttpResponse response = null;

        StringBuilder bbox = new StringBuilder("[[");
        bbox.append(faceDefine.left).append(",").append(faceDefine.top).append(",").append(faceDefine.right).append(",").append(faceDefine.bottom);
        bbox.append("]]");
        try {
            response = Request.Post(Constant.SDK_IP + "/v0/identify")
                    .connectTimeout(10000)
                    .socketTimeout(30000)
                    .addHeader("Authorization", "Token " + Constant.TOKEN)
                    .body(MultipartEntityBuilder
                            .create()
                            //.addTextBody("mf_selector", "all")
                            .addTextBody("bbox",bbox.toString())
                            //.addTextBody("threshold",Constant.IDENTIFY_THRESHOLD)
                            .addBinaryBody("photo", data.mJpgData, ContentType.create("image/jpeg"), "photo.jpg")
                            .build())
                    .execute().returnResponse();
            String reply = EntityUtils.toString(response.getEntity());
            int responseCode = response.getStatusLine().getStatusCode();
            if(responseCode!=200){
                logger.info("请求未正确响应："+responseCode);
                logger.info(reply);
                return null;
            }
            logger.info("SDK-identify:"+reply);
            JSONObject root = (JSONObject) jsonParser.parse(reply);
            if(root.containsKey("results")){
                JSONObject results = (JSONObject) root.get("results");
                JSONArray jsonArray = (JSONArray) results.values().iterator().next();
                int size = jsonArray.size();
                if(size == 0){
                    IdentifyFace identifyFace = new IdentifyFace();
                    identifyFace.setMeta("strange");
                    return identifyFace;
                }
                IdentifyFace identifyFace = new IdentifyFace();
                JSONObject match = (JSONObject) jsonArray.get(0);
                identifyFace.setConfidence((double)match.get("confidence"));
                JSONObject face = (JSONObject) match.get("face");
                identifyFace.setFriendOrFoe((boolean)face.get("friend"));
                identifyFace.setId((long)face.get("id"));
                identifyFace.setMeta((String)face.get("meta"));
                identifyFace.setPersonId((long)face.get("person_id"));
                identifyFace.setTimestamp((String)face.get("timestamp"));
                JSONArray galleries = (JSONArray) face.get("galleries");
                identifyFace.setGalleries(galleries.toJSONString());
                String photoUrl = (String) face.get("normalized");
                byte[] pic = FindFaceUtil.getPicByURL(photoUrl);
                identifyFace.setNormalizedPhoto(pic);
                if(pic==null) {
                    logger.error("ERROR：identifyURL获取图片为null！！！");
                }else {
                    logger.info("identifyURL获取图片长度："+pic.length);
                }
                logger.info("identify成功！");
                return identifyFace;

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }
}

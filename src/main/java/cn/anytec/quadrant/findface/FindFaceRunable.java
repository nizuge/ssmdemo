package cn.anytec.quadrant.findface;

import cn.anytec.quadrant.CameraData.FDCameraData;
import cn.anytec.quadrant.CameraData.FaceDefine;
import cn.anytec.quadrant.CameraDataBootstrap;
import cn.anytec.quadrant.ws.DataPushRunable;
import cn.anytec.quadrant.ws.DataPushScheduler;
import cn.anytec.quadrant.ws.WsMessStore;
import com.sun.imageio.plugins.common.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class FindFaceRunable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(FindFaceRunable.class);
    private static Map<String, List<KnownPerson>> macKPList = new HashMap<>(Constant.CAMERA_AMOUNT);
    private static Map<String, Integer> knownFaceNumMap = new HashMap<>();
    private FDCameraData fdCameraData;
    private Integer knownFaceNum;
    private List<KnownPerson> knownPersonArrayList;
    private Lock lock;
    private Condition condition;

    public FindFaceRunable(FDCameraData data) {
        fdCameraData = data;
        if (!macKPList.containsKey(data.mStrMac)) {
            List<KnownPerson> list = new ArrayList<>();
            macKPList.put(data.mStrMac, list);
        }
        knownFaceNum = knownFaceNumMap.get(data.mStrMac);
        if (knownFaceNum == null)
            knownFaceNum = 0;
    }


    @Override
    public void run() {

        try {
            if (fdCameraData == null) {
                logger.info("线程" + fdCameraData.mJpgSize + "结束");
                FindFaceHandler.setCameraThreadStatus(fdCameraData.mStrMac, false);
                return;
            }

            lock = DataPushScheduler.getLock(fdCameraData.mStrMac);

           /* for (int i = 0; i < fdCameraData.mFaceItem.length; i++) {
                if (fdCameraData.mFaceItem[i].confidence != 0) {
                    logger.info("ID:" + fdCameraData.mFaceItem[i].ID);
                    logger.info("confidence:" + fdCameraData.mFaceItem[i].confidence);
                }
            }*/
            knownPersonArrayList = macKPList.get(fdCameraData.mStrMac);

            int faceNum = fdCameraData.mFaceNum;

            if (faceNum > knownFaceNum) {
                //SDK:detect
                FaceDefine[] faceDefines = FindFaceHandler.getInstance().imageDetect(fdCameraData.mJpgData);
                if (faceDefines == null || faceDefines.length == 0) {
                    logger.info("无效图片");
                    logger.info("线程" + fdCameraData.mJpgSize + "结束");
                    FindFaceHandler.setCameraThreadStatus(fdCameraData.mStrMac, false);
                    return;
                }
                logger.info("new face：" + (faceDefines.length - knownFaceNum));
                logger.info("detectFaceNum:" + faceDefines.length);
                logger.info("knownfaceNum:" + knownFaceNum);
                knownFaceNum = faceDefines.length;
                logger.info("更新knownfaceNum:" + knownFaceNum);
                for (FaceDefine faceDefine : faceDefines) {
                    if (!maybeSame(faceDefine)) {
                        logger.info("创建新的knownface");
                        KnownPerson knownPerson = createKnownPerson(faceDefine);
                        knownPersonArrayList.add(knownPerson);
                        IdentifyFace face = FindFaceHandler.getInstance().imageIdentify(faceDefine, fdCameraData);
                        handleIdentifyInfo(face, knownPerson, faceDefine);
                    }
                }

            } else {
                logger.info("没有新的人脸出现");
                FaceDefine[] faceDefines = FindFaceHandler.getInstance().imageDetect(fdCameraData.mJpgData);
                if (faceDefines == null) {
                    logger.info("线程" + fdCameraData.mJpgSize + "结束");
                    FindFaceHandler.setCameraThreadStatus(fdCameraData.mStrMac, false);
                    return;
                }

                updateKnownPersonList(faceDefines);

            }
            knownFaceNumMap.put(fdCameraData.mStrMac, knownFaceNum);
            FindFaceHandler.setCameraThreadStatus(fdCameraData.mStrMac, false);
            logger.info("线程" + fdCameraData.mJpgSize + "结束");
        } catch (Exception e) {
            FindFaceHandler.setCameraThreadStatus(fdCameraData.mStrMac, false);
            logger.info("线程" + fdCameraData.mJpgSize + "结束");
        }
    }

    private boolean maybeSame(FaceDefine faceDefine) {
        if (knownPersonArrayList.size() == 0) {
            logger.info("无已知人脸");
            return false;
        }
        for (KnownPerson knownPerson : knownPersonArrayList) {
            if (isSamilarFace(faceDefine, knownPerson)) {
                long currentTime = System.currentTimeMillis();
                double appearTime = (currentTime - knownPerson.getCreateTime()) / 1000;
                logger.info("已出现时间：" + appearTime);
                if (appearTime - knownPerson.getAppearTime() > knownPerson.getIdentifyTimeDelay()) {
                    knownPerson.setAppearTime(appearTime);
                    logger.info("有新的人脸：超时重发");
                    IdentifyFace face = FindFaceHandler.getInstance().imageIdentify(faceDefine, fdCameraData);
                    handleIdentifyInfo(face, knownPerson, faceDefine);
                }
                logger.info("相似人脸");
                return true;
            }
        }
        logger.info("不相似人脸");
        return false;
    }

    private void updateKnownPersonList(FaceDefine[] faceDefines) {
        if (knownPersonArrayList.size() == 0) {
            return;
        }
        if (knownPersonArrayList.size() != knownFaceNum)
            knownFaceNum = knownPersonArrayList.size();
    flag:for (int i = 0; i < knownPersonArrayList.size(); i++) {
            KnownPerson knownPerson = knownPersonArrayList.get(i);
            for(FaceDefine faceDefine : faceDefines){
                if (isSamilarFace(faceDefine, knownPerson)) {
                    long currentTime = System.currentTimeMillis();
                    double appearTime = (currentTime - knownPerson.getCreateTime()) / 1000;
                    logger.info("已出现时间：" + appearTime);
                    if (appearTime - knownPerson.getAppearTime() > knownPerson.getIdentifyTimeDelay()) {
                        knownPerson.setAppearTime(appearTime);
                        logger.info("无新的人脸：超时重发");
                        IdentifyFace face = FindFaceHandler.getInstance().imageIdentify(faceDefine, fdCameraData);
                        handleIdentifyInfo(face, knownPerson, faceDefine);
                    }
                    continue flag;
                }
            }

            // knownPersonArrayList.remove(i);
            knownFaceNum--;
            knownPersonArrayList.remove(i);
            logger.info("one knownface miss");
        }
    }

    private void handleIdentifyInfo(IdentifyFace face, KnownPerson knownPerson, FaceDefine faceDefine) {
        if (face == null) {
            logger.error("SDK未正确响应");
            return;
        }
        condition = DataPushScheduler.getCondition(fdCameraData.mStrMac);
        if(condition == null)
            return;

        if (face.getMeta().equals("strange") || face.getConfidence() < Constant.IDENTIFY_THRESHOLD) {
            int times = knownPerson.getTimes();
            times++;
            knownPerson.setTimes(times);
            face.setMeta("陌生访客");
            face.setNormalizedPhoto(cutImage(faceDefine));

            CameraDataBootstrap.getDataPushRunable(fdCameraData.mStrMac).setIdentifyFace(face,fdCameraData.mStrMac);
            //唤醒相应的推送线程
            lock.lock();
            condition.signal();
            lock.unlock();

            if (times == Constant.STRATEGY_STRANGE_TIMES) {
                knownPerson.setTimes(0);
                knownPerson.setIdentifyTimeDelay(Constant.STRATEGY_STRANGE_DELAY);
                return;
            }
            knownPerson.setIdentifyTimeDelay(Constant.STRATEGY_DELAY_UNVERIFIED);
        } else {
            CameraDataBootstrap.getDataPushRunable(fdCameraData.mStrMac).setIdentifyFace(face,fdCameraData.mStrMac);
            //唤醒相应的推送线程
            lock.lock();
            condition.signal();
            lock.unlock();

            knownPerson.setMeta(face.getMeta());
            knownPerson.setIdentifyTimeDelay(Constant.STRATEGY_DELAY_VERIFIED);
            logger.info("已识别：" + face.getMeta());
        }
        //FindFaceHandler.getInstance().requestAnytecSDK(faceDefine,fdCameraData,new String[]{"old",knownPerson.getAppearTime()+""});
    }


    private boolean isSamilarFace(FaceDefine faceDefine, KnownPerson knownPerson) {
        if (isSuitArea(faceDefine, knownPerson)) {
            logger.info("大小匹配");
        } else {
            logger.info("大小不匹配");
            return false;
        }
        if (isSuitCoordinate(faceDefine, knownPerson)) {
            logger.info("左上角坐标匹配");
            return true;
        }
        logger.info("坐标不匹配");
        return false;
    }

    private boolean isSuitArea(FaceDefine faceDefine, KnownPerson knownPerson) {
        double z = Math.sqrt(Math.pow(faceDefine.right - faceDefine.left, 2) + Math.pow(faceDefine.bottom - faceDefine.top, 2));
        double kz = knownPerson.getZ();
        double ratio = z / kz;
        if (ratio > Constant.STRATEGY_FACE_RATIO && ratio < 1 / Constant.STRATEGY_FACE_RATIO) {
            knownPerson.setZ(z);
            return true;
        }
        return false;
    }

    private boolean isSuitCoordinate(FaceDefine faceDefine, KnownPerson knownPerson) {
        double deviation = Math.sqrt(Math.pow(faceDefine.left - knownPerson.getX(), 2) + Math.pow(faceDefine.top - knownPerson.getY(), 2));
        double threshold = (faceDefine.right - faceDefine.left) * Constant.STRATEGY_FACE_RATIO;
        if (deviation < threshold * 0.75) {
            knownPerson.setX(faceDefine.left);
            knownPerson.setY(faceDefine.top);
            return true;
        }
        return false;
    }

    private KnownPerson createKnownPerson(FaceDefine faceDefine) {
        double z = Math.sqrt(Math.pow(faceDefine.right - faceDefine.left, 2) + Math.pow(faceDefine.bottom - faceDefine.top, 2));
        KnownPerson knownPerson = new KnownPerson(faceDefine.left, faceDefine.top, z);
        return knownPerson;
    }

    private byte[] cutImage(FaceDefine faceDefine) {
        int x1 = Double.valueOf(faceDefine.left).intValue();
        int x2 = Double.valueOf(faceDefine.right).intValue();
        int y1 = Double.valueOf(faceDefine.top).intValue();
        int y2 = Double.valueOf(faceDefine.bottom).intValue();
        int width = x2 - x1;
        int height = y2 - y1;
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(fdCameraData.mJpgData);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(Constant.PIC_FORMAT);
            ImageReader reader = readers.next();
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(in);
            reader.setInput(imageInputStream, true);
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rect = new Rectangle(x1, y1, width, height);
            param.setSourceRegion(rect);
            BufferedImage bufferedImage = reader.read(0, param);
            return imageToBytes(bufferedImage, Constant.PIC_FORMAT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] imageToBytes(BufferedImage bImage, String format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, format, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}

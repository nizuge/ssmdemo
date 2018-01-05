package cn.anytec.quadrant.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FindFaceUtil {

    private static final Logger logger = LoggerFactory.getLogger(FindFaceUtil.class);
    private static HttpUtil httpUtil = new HttpUtil();

    public static byte[] getPicByURL(String url){
       /* HttpResponse response = null;
        byte[] pic = null;
        try {
            response = Request.Post(url)
                    .connectTimeout(6000)
                    .socketTimeout(30000)
                    .execute().returnResponse();
            pic = EntityUtils.toByteArray(response.getEntity());
            logger.debug("identify_pic:"+pic.length);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return pic;*/
        byte[] reply = null;
        try {
            reply = httpUtil.getBinaryData(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reply;
    }
}

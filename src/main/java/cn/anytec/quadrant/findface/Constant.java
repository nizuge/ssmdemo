package cn.anytec.quadrant.findface;

import cn.anytec.quadrant.util.ConfigManager;

public class Constant {
    private static ConfigManager sdk = ConfigManager.getInstance();
    public static final String TOKEN = sdk.getParameter("TOKEN");
    public static final String SDK_IP = sdk.getParameter("SDK_IP");

    public static final double STRATEGY_TIME_DELAY = Double.valueOf(sdk.getParameter("STRATEGY_TIME_DELAY"));
    public static final int CAMERA_AMOUNT = Integer.valueOf(sdk.getParameter("CAMERA_AMOUNT"));
    public static final int STRATEGY_CAMERA_SCALE = Integer.valueOf(sdk.getParameter("STRATEGY_CAMERA_SCALE"));
    public static final double STRATEGY_DELAY_UNVERIFIED = Double.valueOf(sdk.getParameter("STRATEGY_DELAY_UNVERIFIED"));
    public static final double STRATEGY_DELAY_VERIFIED = Double.valueOf(sdk.getParameter("STRATEGY_DELAY_VERIFIED"));
    public static final int STRATEGY_STRANGE_TIMES = Integer.valueOf(sdk.getParameter("STRATEGY_STRANGE_TIMES"));
    public static final double STRATEGY_STRANGE_DELAY = Double.valueOf(sdk.getParameter("STRATEGY_STRANGE_DELAY"));

   /* public static final int WIDTH = Integer.valueOf(sdk.getParameter("WIDTH"));
    public static final int HEIGHT = Integer.valueOf(sdk.getParameter("HEIGHT"));*/
    public static final String PIC_FORMAT = sdk.getParameter("PIC_FORMAT");


    public static final double STRATEGY_FACE_RATIO = Double.valueOf(sdk.getParameter("STRATEGY_FACE_RATIO"));

    public static final double IDENTIFY_THRESHOLD = Double.valueOf(sdk.getParameter("IDENTIFY_THRESHOLD"));
}

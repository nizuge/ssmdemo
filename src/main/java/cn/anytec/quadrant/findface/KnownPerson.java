package cn.anytec.quadrant.findface;

public class KnownPerson {
    private double z;
    private double x;
    private double y;
    private long createTime;
    private double appearTime;
    private double identifyTimeDelay;
    private int times;
    private String meta;

    public KnownPerson(double x,double y,double z){
        this.z = z;
        this.x = x;
        this.y = y;
        createTime = System.currentTimeMillis();
        appearTime = 0;
        meta = null;
        identifyTimeDelay = Constant.STRATEGY_TIME_DELAY;
    }

    public long getCreateTime() {
        return createTime;
    }

    public double getAppearTime() {
        return appearTime;
    }

    public void setAppearTime(double appearTime) {
        this.appearTime = appearTime;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getIdentifyTimeDelay() {
        return identifyTimeDelay;
    }

    public void setIdentifyTimeDelay(double identifyTimeDelay) {
        this.identifyTimeDelay = identifyTimeDelay;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}

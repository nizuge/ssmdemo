package cn.anytec.quadrant.CameraControl;

public class CameraLiveViewer {
    private String camMac;
    private boolean online;

    public CameraLiveViewer(String camMac,boolean online){
        this.camMac = camMac;
        this.online = online;
    }

    public String getCamMac() {
        return camMac;
    }

    public void setCamMac(String camMac) {
        this.camMac = camMac;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}

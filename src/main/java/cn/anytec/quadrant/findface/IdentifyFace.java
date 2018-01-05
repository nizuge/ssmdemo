package cn.anytec.quadrant.findface;

public class IdentifyFace {
    private long id;
    private double confidence;
    private long personId;
    private String galleries;
    private String meta;
    private boolean friendOrFoe;
    private byte[] normalizedPhoto;
    private String timestamp;
    private int interviewTimes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getGalleries() {
        return galleries;
    }

    public void setGalleries(String galleries) {
        this.galleries = galleries;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public boolean isFriendOrFoe() {
        return friendOrFoe;
    }

    public void setFriendOrFoe(boolean friendOrFoe) {
        this.friendOrFoe = friendOrFoe;
    }

    public byte[] getNormalizedPhoto() {
        return normalizedPhoto;
    }

    public void setNormalizedPhoto(byte[] normalizedPhoto) {
        this.normalizedPhoto = normalizedPhoto;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getInterviewTimes() {
        return interviewTimes;
    }

    public void setInterviewTimes(int interviewTimes) {
        this.interviewTimes = interviewTimes;
    }
}

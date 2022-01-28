package com.ibm.trl.funqy.multimedia;


public class Param {
    private int height;
    private int width;
    private String objectKey; // 210.thumbnailer uses "objectKey"
    private String key;       // 220.video-processing uses "key"
    private int duration;
    private String op;

    public Param() {}

    public int getHeight() { return height; }
    public void setHeight(int h) { this.height = h; }

    public int getWidth() { return width; }
    public void setWidth(int w) { this.width = w; }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String o) { this.objectKey = o; }

    public String getKey() { return key; }
    public void setKey(String k) { this.key = k; }

    public int getDuration() { return duration; }
    public void setDuration(int d) { this.duration = d; }

    public String getOp() { return op; }
    public void setOp(String o) { this.op = o; }
}

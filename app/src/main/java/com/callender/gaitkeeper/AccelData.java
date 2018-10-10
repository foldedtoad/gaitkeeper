package com.callender.gaitkeeper;

public class AccelData {
    private long timeStamp;
    private double x;
    private double y;
    private double z;

    public AccelData(int id, long timeStamp, double x, double y, double z) {
        this.timeStamp = timeStamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public AccelData(long timeStamp, double x, double y, double z) {
        this.timeStamp = timeStamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) { this.x = x; }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return timeStamp + ", " + x + ", " + y + ", " + z + "\n";
    }
}

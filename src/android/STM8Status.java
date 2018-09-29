package com.mumatech.controller;

public class STM8Status {
    private int battery;
    private boolean leftLockStatus;
    private boolean rightLockStatus;
    private boolean kPadStatus;
    private boolean speakerStatus;
    private String version;

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isLeftLockStatus() {
        return leftLockStatus;
    }

    public void setLeftLockStatus(boolean leftLockStatus) {
        this.leftLockStatus = leftLockStatus;
    }

    public boolean isRightLockStatus() {
        return rightLockStatus;
    }

    public void setRightLockStatus(boolean rightLockStatus) {
        this.rightLockStatus = rightLockStatus;
    }

    public boolean iskPadStatus() {
        return kPadStatus;
    }

    public void setkPadStatus(boolean kPadStatus) {
        this.kPadStatus = kPadStatus;
    }

    public boolean isSpeakerStatus() {
        return speakerStatus;
    }

    public void setSpeakerStatus(boolean speakerStatus) {
        this.speakerStatus = speakerStatus;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static STM8Status initWithData(String data) {
        STM8Status status = new STM8Status();
        String[] statusReport = data.split(":");
        String[] contents = statusReport[1].split(",");
        for (int i = 0; i < contents.length; i++) {
            status.setBattery(Integer.valueOf(contents[0]));
            status.setLeftLockStatus("UNLOCKED".equals(contents[1]));
            status.setLeftLockStatus("UNLOCKED".equals(contents[2]));
            status.setSpeakerStatus("CONNECTED".equals(contents[3]));
            status.setkPadStatus("CONNECTED".equals(contents[4]));
            status.setVersion(contents[8]);
        }
        return status;
    }
}

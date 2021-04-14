package com.dynamsoft.camerax;

public class HistoryRecord {
    String TimeStamp;
    String Code;
    String ImagePath;
    public HistoryRecord(String timeStamp, String code, String path) {
        this.TimeStamp = timeStamp;
        this.Code = code;
        this.ImagePath = path;
    }
}

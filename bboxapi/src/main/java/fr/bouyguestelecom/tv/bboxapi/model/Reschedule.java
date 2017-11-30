package fr.bouyguestelecom.tv.bboxapi.model;

import java.io.Serializable;

public class Reschedule implements Serializable {
    public static final String TAG_START_TIME = "startTime";
    public static final String TAG_END_TIME = "endTime";
    public static final String TAG_LANGUAGE = "language";
    public static final String TAG_EVENT_ID = "eventId";
    public static final String TAG_EPG_CHANNEL_NUMBER = "epgChannelNumber";

    private String startTime;
    private String endTime;
    private String language;
    private String eventId;
    private int epgChannelNumber;

    public Reschedule(String startTime, String endTime, String language, String eventId, int epgChannelNumber) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.language = language;
        this.eventId = eventId;
        this.epgChannelNumber = epgChannelNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Integer getEpgChannelNumber() {
        return epgChannelNumber;
    }

    public String getLanguage() {
        return language;
    }

    public String getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return "Reschedule{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", epgChannelNumber=" + epgChannelNumber +
                ", language='" + language + '\'' +
                ", eventId='" + eventId + '\'' +
                '}';
    }
}

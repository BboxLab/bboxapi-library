package fr.bouyguestelecom.tv.bboxapi.model;

import org.json.JSONException;

public class NotifChannel {

    private String notif_channels;

    public NotifChannel(String channel) throws JSONException {
        this.notif_channels = channel;
    }

    @Override
    public String toString() {
        return "Channels : " + notif_channels;
    }

}
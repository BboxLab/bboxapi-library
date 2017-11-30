package fr.bouyguestelecom.tv.bboxapi.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MediaResource implements Comparable<MediaResource> {

    private String mediaService;
    private String mediaState;
    private String mediaTitle;
    private int positionId;

    public MediaResource(JSONObject jsonObject) {
        try {
            this.positionId = jsonObject != null && jsonObject.has("positionId") && jsonObject.get("positionId") != null && jsonObject.get("positionId") instanceof String ? Integer.parseInt(jsonObject.getString("positionId")) : -1;
            this.mediaService = jsonObject != null && jsonObject.has("mediaService") && jsonObject.get("mediaService") != null && jsonObject.get("mediaService") instanceof String ? jsonObject.getString("mediaService") : null;
            this.mediaState = jsonObject != null && jsonObject.has("mediaState") && jsonObject.get("mediaState") != null && jsonObject.get("mediaState") instanceof String ? jsonObject.getString("mediaState") : null;
            this.mediaTitle = jsonObject != null && jsonObject.has("mediaTitle") && jsonObject.get("mediaTitle") != null && jsonObject.get("mediaTitle") instanceof String ? jsonObject.getString("mediaTitle") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getPositionId() {
        return positionId;
    }

    public String getMediaService() {
        return mediaService;
    }

    public String getMediaState() {
        return mediaState;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    @Override
    public String toString() {
        return "MediaResource{" +
                "mediaService='" + mediaService + '\'' +
                ", mediaState='" + mediaState + '\'' +
                ", mediaTitle='" + mediaTitle + '\'' +
                ", positionId=" + positionId +
                '}';
    }

    @Override
    public int compareTo(MediaResource channel) {
        if (channel.getPositionId() > positionId)
            return -1;

        else if (positionId > channel.getPositionId())
            return 1;

        else
            return 0;
    }
}
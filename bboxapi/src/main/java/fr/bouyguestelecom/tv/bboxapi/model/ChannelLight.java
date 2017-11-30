package fr.bouyguestelecom.tv.bboxapi.model;

import android.util.JsonReader;
import android.util.JsonToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChannelLight implements Comparable<ChannelLight> {

    private String mediaState;
    private String mediaTitle;
    private int positionId;

    public ChannelLight(JsonReader reader) {
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {

                    case "mediaState":
                        if (reader.peek() == JsonToken.STRING)
                            this.mediaState = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "mediaTitle":
                        if (reader.peek() == JsonToken.STRING)
                            this.mediaTitle = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "positionId":
                        if (reader.peek() != JsonToken.NULL)
                            positionId = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ChannelLight(JSONObject jsonObject) {
        try {
            this.positionId = jsonObject != null && jsonObject.has("positionId") && jsonObject.get("positionId") != null && jsonObject.get("positionId") instanceof Integer ? jsonObject.getInt("positionId") : -1;
            } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getPositionId() {
        return positionId;
    }

    public String getMediaState() {
        return mediaState;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    @Override
    public String toString() {
        return "Channel{" +
                ", mediaState='" + mediaState + '\'' +
                ", mediaTitle='" + mediaTitle + '\'' +
                ", positionId=" + positionId +
                '}';
    }

    @Override
    public int compareTo(ChannelLight channel) {
        if (channel.getPositionId() > positionId)
            return -1;

        else if (positionId > channel.getPositionId())
            return 1;

        else
            return 0;
    }
}
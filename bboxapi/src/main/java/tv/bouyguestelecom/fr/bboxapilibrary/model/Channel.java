package tv.bouyguestelecom.fr.bboxapilibrary.model;

import android.util.JsonReader;
import android.util.JsonToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Channel implements Comparable<Channel> {

    private String name;
    private String logo;
    private String mediaService;
    private String mediaState;
    private String mediaTitle;
    private int positionId;
    private int positionIdBbox;

    public Channel(JsonReader reader) {
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "name":
                        if (reader.peek() == JsonToken.STRING)
                            this.name = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "logo":
                        if (reader.peek() == JsonToken.STRING)
                            this.logo = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "mediaService":
                        if (reader.peek() == JsonToken.STRING)
                            this.mediaService = reader.nextString();
                        else
                            reader.skipValue();
                        break;

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

                    case "positionIdBbox":
                        if (reader.peek() != JsonToken.NULL)
                            positionIdBbox = reader.nextInt();
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

    public Channel(JSONObject jsonObject) {
        try {
            this.name = jsonObject != null && jsonObject.has("name") && jsonObject.get("name") != null && jsonObject.get("name") instanceof String ? jsonObject.getString("name") : null;
            this.positionId = jsonObject != null && jsonObject.has("positionId") && jsonObject.get("positionId") != null && jsonObject.get("positionId") instanceof Integer ? jsonObject.getInt("positionId") : -1;
            this.positionIdBbox = jsonObject != null && jsonObject.has("positionIdBbox") && jsonObject.get("positionIdBbox") != null && jsonObject.get("positionIdBbox") instanceof Integer ? jsonObject.getInt("positionIdBbox") : -1;
            this.logo = jsonObject != null && jsonObject.has("logo") && jsonObject.get("logo") != null && jsonObject.get("logo") instanceof String ? jsonObject.getString("logo") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public int getPositionId() {
        return positionId;
    }

    public String getLogo() {
        return logo;
    }

    public int getPositionIdBbox() {
        return positionIdBbox;
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
        return "Channel{" +
                "name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", mediaService='" + mediaService + '\'' +
                ", mediaState='" + mediaState + '\'' +
                ", mediaTitle='" + mediaTitle + '\'' +
                ", positionId=" + positionId +
                ", positionIdBbox=" + positionIdBbox +
                '}';
    }

    @Override
    public int compareTo(Channel channel) {
        if (channel.getPositionId() > positionId)
            return -1;

        else if (positionId > channel.getPositionId())
            return 1;

        else
            return 0;
    }
}
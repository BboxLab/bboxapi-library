package tv.bouyguestelecom.fr.bboxapilibrary.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

public class Channel implements Parcelable, Comparable<Channel> {

    private String name;
    private String logo;
    private String theme;
    private String url;
    private int positionId;
    private int epgChannelNumber;
    private String mediaState;

    public Channel() {

    }

    public Channel(JsonReader reader) {
        try {
            if (reader.peek() == JsonToken.BEGIN_OBJECT)
                reader.beginObject();

            else
                return;

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

                    case "theme":
                        if (reader.peek() == JsonToken.STRING)
                            this.theme = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "url":
                        if (reader.peek() == JsonToken.STRING)
                            this.url = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "positionId":
                        if (reader.peek() != JsonToken.NULL)
                            positionId = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    case "epgChannelNumber":
                        if (reader.peek() != JsonToken.NULL)
                            this.epgChannelNumber = reader.nextInt();
                        else
                            reader.skipValue();
                        break;
                    case "mediaService":
                        if (reader.peek() != JsonToken.NULL)
                            this.url = reader.nextString();
                        else
                            reader.skipValue();
                        break;
                    case "mediaTitle":
                        if (reader.peek() != JsonToken.NULL)
                            this.name = reader.nextString();
                        else
                            reader.skipValue();
                        break;
                    case "mediaState":
                        if (reader.peek() == JsonToken.STRING)
                            this.mediaState = reader.nextString();
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

    protected Channel(Parcel in) {
        name = in.readString();
        logo = in.readString();
        theme = in.readString();
        url = in.readString();
        positionId = in.readInt();
        epgChannelNumber = in.readInt();
        mediaState = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(logo);
        dest.writeString(theme);
        dest.writeString(url);
        dest.writeInt(positionId);
        dest.writeInt(epgChannelNumber);
        dest.writeString(mediaState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public int getEpgChannelNumber() {
        return epgChannelNumber;
    }

    public void setEpgChannelNumber(int epgChannelNumber) {
        this.epgChannelNumber = epgChannelNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMediaState() {
        return mediaState;
    }

    public void setMediaState(String mediaState) {
        this.mediaState = mediaState;
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

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", theme='" + theme + '\'' +
                ", url='" + url + '\'' +
                ", positionId=" + positionId +
                ", epgChannelNumber=" + epgChannelNumber +
                ", mediaState='" + mediaState + '\'' +
                '}';
    }
}
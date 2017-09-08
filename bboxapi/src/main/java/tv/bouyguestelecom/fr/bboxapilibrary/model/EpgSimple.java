package tv.bouyguestelecom.fr.bboxapilibrary.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

public class EpgSimple implements Parcelable, Comparable<EpgSimple>{

    private String eventId;
    private String externalId;
    private String title;
    private String genre;
    private String startTime;
    private String endTime;
    private String thumb;
    private int epgChannelNumber;
    private int positionId;

    public EpgSimple(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT)
            reader.beginObject();

        else
            reader.skipValue();

        while (reader.hasNext()) {
            String name;

            if (reader.peek() == JsonToken.NAME) {
                name = reader.nextName();

                switch (name) {
                    case "posterArtUri":
                    case "thumbnailUri":
                    case "thumb":
                        if (reader.peek() == JsonToken.STRING)
                            thumb = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "title":
                        if (reader.peek() == JsonToken.STRING)
                            title = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "broadcastGenre":
                        if (reader.peek() == JsonToken.STRING) {
                            genre = reader.nextString();
                            genre = genre.substring(genre.lastIndexOf(',')+1);
                        } else
                            reader.skipValue();
                        break;

                    case "genre":
                        if (reader.peek() == JsonToken.STRING)
                            genre = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "programId":
                        if (reader.peek() == JsonToken.NUMBER)
                            eventId = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "eventId":
                        if (reader.peek() == JsonToken.STRING)
                            eventId = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "externalId":
                        if (reader.peek() == JsonToken.STRING)
                            externalId = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "startTime":
                        if (reader.peek() == JsonToken.STRING)
                            startTime = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "endTime":
                        if (reader.peek() == JsonToken.STRING)
                            endTime = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "channelId":
                    case "epgChannelNumber":
                        if (reader.peek() == JsonToken.NUMBER)
                            epgChannelNumber = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    case "positionId":
                        if (reader.peek() == JsonToken.NUMBER)
                            positionId = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    default:
                        reader.skipValue();
                        break;
                }
            } else if(reader.peek() != JsonToken.END_DOCUMENT)
                reader.skipValue();
            else
                break;
        }

        if (reader.peek() == JsonToken.END_OBJECT) {
            reader.endObject();
        }

        else if(reader.peek() != JsonToken.END_DOCUMENT)
            reader.skipValue();
    }

    protected EpgSimple(Parcel in) {
        eventId = in.readString();
        externalId = in.readString();
        title = in.readString();
        genre = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        thumb = in.readString();
        epgChannelNumber = in.readInt();
        positionId = in.readInt();
    }

    public static final Creator<EpgSimple> CREATOR = new Creator<EpgSimple>() {
        @Override
        public EpgSimple createFromParcel(Parcel in) {
            return new EpgSimple(in);
        }

        @Override
        public EpgSimple[] newArray(int size) {
            return new EpgSimple[size];
        }
    };

    @Override
    public int compareTo(EpgSimple epgSimple) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(eventId);
        parcel.writeString(externalId);
        parcel.writeString(title);
        parcel.writeString(genre);
        parcel.writeString(startTime);
        parcel.writeString(endTime);
        parcel.writeString(thumb);
        parcel.writeInt(epgChannelNumber);
        parcel.writeInt(positionId);
    }

    public String getEventId() {
        return eventId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getThumb() {
        return thumb;
    }

    public int getEpgChannelNumber() {
        return epgChannelNumber;
    }

    public int getPositionId() {
        return positionId;
    }

    @Override
    public String toString() {
        return "EpgSimple{" +
                "eventId='" + eventId + '\'' +
                ", externalId='" + externalId + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", thumb='" + thumb + '\'' +
                ", epgChannelNumber=" + epgChannelNumber +
                ", positionId=" + positionId +
                '}';
    }
}
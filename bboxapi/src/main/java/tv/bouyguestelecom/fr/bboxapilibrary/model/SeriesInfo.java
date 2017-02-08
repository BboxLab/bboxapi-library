package tv.bouyguestelecom.fr.bboxapilibrary.model;


import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.Serializable;

public class SeriesInfo implements Serializable {
    private String seriesName;
    private int seasonNumber;
    private int episodeNumber;
    private int totalEpisodeNumber;
    private boolean seasonPremiere;

    public SeriesInfo(JsonReader reader) {
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "seriesName":
                        if (reader.peek() == JsonToken.STRING)
                            this.seriesName = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "seasonNumber":
                        if (reader.peek() != JsonToken.NULL)
                            this.seasonNumber = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    case "episodeNumber":
                        if (reader.peek() != JsonToken.NULL)
                            this.episodeNumber = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    case "totalEpisodeNumber":
                        if (reader.peek() != JsonToken.NULL)
                            this.totalEpisodeNumber = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    case "seasonPremiere":
                        if (reader.peek() == JsonToken.BOOLEAN)
                            this.seasonPremiere = reader.nextBoolean();
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

    public String getSeriesName() {
        return seriesName;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public int getTotalEpisodeNumber() {
        return totalEpisodeNumber;
    }

    public boolean isSeasonPremiere() {
        return seasonPremiere;
    }

    @Override
    public String toString() {
        return "SeriesInfo{" +
                "seriesName='" + seriesName + '\'' +
                ", seasonNumber=" + seasonNumber +
                ", episodeNumber=" + episodeNumber +
                ", totalEpisodeNumber=" + totalEpisodeNumber +
                ", seasonPremiere=" + seasonPremiere +
                '}';
    }
}

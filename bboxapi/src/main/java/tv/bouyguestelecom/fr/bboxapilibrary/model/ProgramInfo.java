package tv.bouyguestelecom.fr.bboxapilibrary.model;


import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProgramInfo implements Serializable {
    private String shortTitle;
    private String longTitle;
    private String secondaryTitle;
    private String shortSummary;
    private String longSummary;
    private String characterDisplay;
    private String provider;
    private String externalId;
    private String duration;
    private String countryOfOrigin;
    private int productionDate;
    private double publicRank;
    private double pressRank;
    private boolean changed;
    private List<String> genres = new ArrayList<>();
    private List<String> subGenres = new ArrayList<>();
    private List<String> keyword = new ArrayList<>();
    private List<Character> characters = new ArrayList<>();

    public ProgramInfo(JsonReader reader) {
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "shortTitle":
                        if (reader.peek() == JsonToken.STRING)
                            shortTitle = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "longTitle":
                        if (reader.peek() == JsonToken.STRING)
                            longTitle = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "secondaryTitle":
                        if (reader.peek() == JsonToken.STRING)
                            secondaryTitle = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "shortSummary":
                        if (reader.peek() == JsonToken.STRING)
                            shortSummary = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "longSummary":
                        if (reader.peek() == JsonToken.STRING)
                            longSummary = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "characterDisplay":
                        if (reader.peek() == JsonToken.STRING)
                            characterDisplay = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "provider":
                        if (reader.peek() == JsonToken.STRING)
                            provider = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "externalId":
                        if (reader.peek() == JsonToken.STRING)
                            externalId = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "changed":
                        if (reader.peek() == JsonToken.BOOLEAN)
                            changed = reader.nextBoolean();
                        else
                            reader.skipValue();
                        break;

                    case "duration":
                        if (reader.peek() == JsonToken.STRING)
                            duration = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "countryOfOrigin":
                        if (reader.peek() == JsonToken.STRING)
                            countryOfOrigin = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "productionDate":
                        if (reader.peek() != JsonToken.NULL)
                            productionDate = reader.nextInt();
                        else
                            reader.skipValue();
                        break;

                    case "publicRank":
                        if (reader.peek() != JsonToken.NULL)
                            publicRank = reader.nextDouble();
                        else
                            reader.skipValue();
                        break;

                    case "pressRank":
                        if (reader.peek() != JsonToken.NULL)
                            pressRank = reader.nextDouble();
                        else
                            reader.skipValue();
                        break;

                    case "character":
                        if (reader.peek() != JsonToken.NULL) {
                            characters = getCharacters(reader);
                        } else
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

    private List<Character> getCharacters(JsonReader reader) {
        List<Character> characters = new ArrayList<>();

        try {
            reader.beginArray();

            String firstName = null;
            String lastName = null;
            String role = null;
            String function = null;
            int rank = -1;

            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.BEGIN_OBJECT)
                    reader.beginObject();

                String name = reader.nextName();

                switch (name) {
                    case Character.TAG_FIRSTNAME:
                        if (reader.peek() == JsonToken.STRING)
                            firstName = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case Character.TAG_LASTNAME:
                        if (reader.peek() == JsonToken.STRING)
                            lastName = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case Character.TAG_ROLE:
                        if (reader.peek() == JsonToken.STRING)
                            role = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case Character.TAG_FUNCTION:
                        if (reader.peek() == JsonToken.STRING)
                            function = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case Character.TAG_RANK:
                        rank = reader.nextInt();
                        break;

                    default:
                        reader.skipValue();
                        break;
                }

                if (reader.peek() == JsonToken.END_OBJECT) {
                    reader.endObject();
                    characters.add(new Character(firstName, lastName, role, function, rank));
                }
            }

            reader.endArray();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(characters);

        return characters;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public String getLongTitle() {
        return longTitle;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public String getShortSummary() {
        return shortSummary;
    }

    public String getExternalId() {
        return externalId;
    }

    public boolean getChanged() {
        return changed;
    }

    public String getDuration() {
        return duration;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public int getProductionDate() {
        return productionDate;
    }

    public double getPublicRank() {
        return publicRank;
    }

    public double getPressRank() {
        return pressRank;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getSubGenres() {
        return subGenres;
    }

    public String getLongSummary() {
        return longSummary;
    }

    public String getCharacterDisplay() {
        return characterDisplay;
    }

    public String getProvider() {
        return provider;
    }

    public List<String> getKeyword() {
        return keyword;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    @Override
    public String toString() {
        return "ProgramInfo{" +
                "shortTitle='" + shortTitle + '\'' +
                ", longTitle='" + longTitle + '\'' +
                ", secondaryTitle='" + secondaryTitle + '\'' +
                ", shortSummary='" + shortSummary + '\'' +
                ", longSummary='" + longSummary + '\'' +
                ", characterDisplay='" + characterDisplay + '\'' +
                ", provider='" + provider + '\'' +
                ", externalId='" + externalId + '\'' +
                ", changed='" + changed + '\'' +
                ", duration='" + duration + '\'' +
                ", countryOfOrigin='" + countryOfOrigin + '\'' +
                ", productionDate=" + productionDate +
                ", publicRank=" + publicRank +
                ", pressRank=" + pressRank +
                ", genres=" + genres +
                ", subGenres=" + subGenres +
                ", keyword=" + keyword +
                ", characters=" + characters +
                '}';
    }
}

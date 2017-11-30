package fr.bouyguestelecom.tv.bboxapi.model;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

public class Application implements Comparable<Application> {
    private String appName;
    private String appId;
    private String packageName;
    private String appState;
    private String component;
    private String data;
    private String action;
    private boolean leanback;
    private String logoUrl;

    public Application(String ip, JsonReader reader) {
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "appName":
                        if (reader.peek() == JsonToken.STRING)
                            this.appName = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "appId":
                        if (reader.peek() == JsonToken.STRING)
                            this.appId = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "packageName":
                        if (reader.peek() == JsonToken.STRING)
                            this.packageName = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "appState":
                        if (reader.peek() == JsonToken.STRING)
                            this.appState = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "component":
                        if (reader.peek() == JsonToken.STRING)
                            this.component = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "data":
                        if (reader.peek() == JsonToken.STRING)
                            this.data = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "action":
                        if (reader.peek() == JsonToken.STRING)
                            this.action = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "logoUrl":
                        if (reader.peek() == JsonToken.STRING)
                            if (ip != null && !ip.isEmpty())
                                this.logoUrl = ip + reader.nextString();
                            else
                                this.logoUrl = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "leanback":
                        if (reader.peek() == JsonToken.BOOLEAN)
                            leanback = reader.nextBoolean();
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

    public String getAppName() {
        return appName;
    }

    public String getAppId() {
        return appId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppState() {
        return appState;
    }

    public String getComponent() {
        return component;
    }

    public String getData() {
        return data;
    }

    public String getAction() {
        return action;
    }

    public boolean isLeanback() {
        return leanback;
    }

    public String getUrlLogo() {
        return logoUrl;
    }

    public void setLogo(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @Override
    public String toString() {
        return "Application{" +
                "appName='" + appName + '\'' +
                ", appId='" + appId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appState='" + appState + '\'' +
                ", component='" + component + '\'' +
                ", data='" + data + '\'' +
                ", action='" + action + '\'' +
                ", leanback=" + leanback +
                '}';
    }

    @Override
    public int compareTo(Application another) {
        if (another != null) {
            return appName.toLowerCase().compareTo(another.getAppName().toLowerCase());
        }

        return 0;
    }
}

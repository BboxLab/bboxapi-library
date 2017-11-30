package fr.bouyguestelecom.tv.bboxapi.model;

import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;

public class Resource {
    private String resourceId;
    private String body;
    private String error;

    public Resource(JsonReader reader) {
        try {
            reader.beginObject();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "resourceId":
                        if (reader.peek() == JsonToken.STRING)
                            this.resourceId = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "body":
                        if (reader.peek() == JsonToken.STRING)
                            this.body = reader.nextString();
                        else
                            reader.skipValue();
                        break;

                    case "error":
                        if (reader.peek() == JsonToken.STRING)
                            this.error = reader.nextString();
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


    public String getResourceId() {
        return resourceId;
    }

    public String getBody() {
        return body;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceId='" + resourceId + '\'' +
                ", body='" + body + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}

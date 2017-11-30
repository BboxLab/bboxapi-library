package fr.bouyguestelecom.tv.bboxapi.model;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageResource {

    private String appId;
    private String message;

    public MessageResource(String appId, String message) {
        this.appId = appId;
        this.message = message;
    }

    public MessageResource(JSONObject jsonObject) {
        try {
            this.appId = jsonObject != null && jsonObject.has("appId") && jsonObject.get("appId") != null && jsonObject.get("appId") instanceof String ? jsonObject.getString("appId") : null;
            this.message = jsonObject != null && jsonObject.has("message") && jsonObject.get("message") != null && jsonObject.get("message") instanceof String ? jsonObject.getString("message") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getAppId() {
        return appId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{" +
                "\"appId\": \"" + appId + '\"' +
                ", \"message\": \"" + message + '\"' +
                '}';
    }
}
package fr.bouyguestelecom.tv.bboxapi.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplicationResource {

    private String packageName;
    private String state;

    public ApplicationResource(String packageName, String state) {
        this.packageName = packageName;
        this.state = state;
    }

    public ApplicationResource(JSONObject jsonObject) {
        try {
            this.packageName = jsonObject != null && jsonObject.has("packageName") && jsonObject.get("packageName") != null && jsonObject.get("packageName") instanceof String ? jsonObject.getString("packageName") : null;
            this.state = jsonObject != null && jsonObject.has("state") && jsonObject.get("state") != null && jsonObject.get("state") instanceof String ? jsonObject.getString("state") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public String getState() {
        return state;
    }

    @Override
    public String toString() {
        return "{" +
                "\"packageName\": \"" + packageName + '\"' +
                ", \"state\": \"" + state + '\"' +
                '}';
    }
}
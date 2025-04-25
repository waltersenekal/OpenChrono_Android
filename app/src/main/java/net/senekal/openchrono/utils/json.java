package net.senekal.openchrono.utils;

import android.util.Log;

import org.json.JSONObject;

public class json {
    private static final String TAG = "O_Json";
    private JSONObject jsonObject;

    public json(json oJson) {
        if(oJson !=null) {
            this.jsonObject = oJson.jsonObject;
        }
    }
    public json(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    public json(String jsonString) {
        this.jsonObject = parse(jsonString);
    }


    public String getValueAsString(String jsonElement) {
        if (this.jsonObject != null && jsonElement != null && !jsonElement.isEmpty()) {
            try {
                return this.jsonObject.getString(jsonElement);
            } catch (Exception e) {
                Log.e(TAG, "Error extracting JSON element: ", e);
                return "";
            }
        } else {
            Log.e(TAG, "Invalid input: jsonObject is null or jsonElement is null/empty");
            return "";
        }
    }


    public JSONObject parse(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: ", e);
            return null;
        }
    }

}
package com.palashbari.project;

import android.content.Context;

import com.android.volley.VolleyError;
import com.palashbari.project.util.API;
import com.palashbari.project.util.JsonAPICall;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TokenManager {

    public interface TokenCallback {
        void onTokenRefreshed(String newToken);
        void onTokenUnavailable();
    }

    public static void regenerateToken(Context context, String refreshToken, TokenCallback callback) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            callback.onTokenUnavailable();
            return;
        }

        Map<String, String> formData = new HashMap<>();
        formData.put("refresh_token", refreshToken);

        UserManagement userManagement = new UserManagement(context);

        JsonAPICall.makePostRequest(context, API.token_regenerate, formData, new JsonAPICall.VolleyCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    System.out.println("token :"+jsonObject);
                    if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
                        JSONObject dataObj = jsonObject.getJSONObject("data");
                        String newToken = dataObj.getString("access_token");
                        String newRefreshToken = dataObj.getString("refresh_token");

                        // ✅ Save new tokens
                        userManagement.token(newToken, newRefreshToken);

                        // ✅ Callback success
                        callback.onTokenRefreshed(newToken);
                    } else {
                        callback.onTokenUnavailable();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onTokenUnavailable();
                }
            }

            @Override
            public void onError(VolleyError error) {
                if (error.networkResponse != null && (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 404)) {
                    userManagement.logout(context);
                } else {
                    callback.onTokenUnavailable();
                }

                System.out.println("token :"+error);
            }
        });
    }
}

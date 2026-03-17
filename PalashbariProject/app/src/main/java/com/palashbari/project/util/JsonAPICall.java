package com.palashbari.project.util;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.palashbari.project.TokenManager;
import com.palashbari.project.UserManagement;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonAPICall {

    // ============ GENERIC EXECUTION WRAPPER ============
    private static void executeRequestWithToken(Context context, Runnable requestExecutor, VolleyCallback callback, VolleyError error) {
        if (error != null && error.networkResponse != null && error.networkResponse.statusCode == 401) {
            handleTokenExpiry(context, requestExecutor, callback);
        } else {
            if (error != null) callback.onError(error);
        }
    }

    private static void handleTokenExpiry(Context context, Runnable retryRequest, VolleyCallback callback) {
        UserManagement userManagement = new UserManagement(context);
        String refreshToken = userManagement.userDetails().get(userManagement.REFRESH_TOKEN);

        TokenManager.regenerateToken(context, refreshToken, new TokenManager.TokenCallback() {
            @Override
            public void onTokenRefreshed(String newToken) {
                retryRequest.run();
            }

            @Override
            public void onTokenUnavailable() {
                userManagement.logout(context);
            }
        });
    }

    // ============ GET REQUEST ============
    public static void makeGetRequestWithToken(Context context, String url, final VolleyCallback callback) {
        UserManagement userManagement = new UserManagement(context);
        String token = userManagement.userDetails().get(UserManagement.TOKEN);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                callback::onSuccess,
                error -> executeRequestWithToken(context,
                        () -> makeGetRequestWithToken(context, url, callback),
                        callback,
                        error)) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }


    // ============ POST REQUEST (JSON BODY) ============
    public static void makePostRequestWithToken(Context context, String url, JSONObject jsonBody, final VolleyCallback callback) {
        UserManagement userManagement = new UserManagement(context);
        String token = userManagement.userDetails().get(UserManagement.TOKEN);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> callback.onSuccess(response.toString()),
                error -> executeRequestWithToken(context,
                        () -> makePostRequestWithToken(context, url, jsonBody, callback),
                        callback,
                        error)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }


    // ============ POST REQUEST (FORM DATA) ============
    public static void makePostRequestWithTokenAndFormData(Context context, String url, final Map<String, String> formData, final VolleyCallback callback) {
        UserManagement userManagement = new UserManagement(context);
        String token = userManagement.userDetails().get(UserManagement.TOKEN);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                callback::onSuccess,
                error -> executeRequestWithToken(context,
                        () -> makePostRequestWithTokenAndFormData(context, url, formData, callback),
                        callback,
                        error)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return formData;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }


    // ============ NORMAL POST REQUEST ============
    public static void makePostRequest(Context context, String url, final Map<String, String> parameters, final VolleyCallback callback) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                callback::onSuccess,
                callback::onError) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return parameters;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    // ============ CALLBACK INTERFACE ============
    public interface VolleyCallback {
        void onSuccess(String result);
        void onError(VolleyError error);
    }
}

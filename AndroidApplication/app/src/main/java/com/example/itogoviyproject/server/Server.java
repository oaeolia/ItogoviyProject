package com.example.itogoviyproject.server;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.itogoviyproject.loggers.ILogger;

import org.json.JSONException;
import org.json.JSONObject;

public class Server {
    private static final String SERVER_URL = "http://192.168.1.13:5000/api/v1/";
    private static final String SERVER_RESPONSE_OK = "OK";
    private static final String SERVER_RESPONSE_BAD = "BAD";
    private static final String SERVER_RESPONSE_ERROR = "ERROR";


    private final RequestQueue requestQueue;
    private final ILogger logger;

    private int sessionId = -1;
    private String sessionToken = "";


    public Server(Context context, ILogger logger) {
        requestQueue = Volley.newRequestQueue(context);
        this.logger = logger;
    }

    public void registration(String name, String email, String password, ServerCallback<Boolean, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/registration", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(true, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    logger.logError("Server", "Server undefined error (undefined status, try registration)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (registration): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t registration " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t registration " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error", null, null);
            }
        });

        requestQueue.add(request);
    }
}

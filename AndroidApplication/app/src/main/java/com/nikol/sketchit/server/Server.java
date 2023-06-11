package com.nikol.sketchit.server;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nikol.sketchit.Application;
import com.nikol.sketchit.loggers.ILogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server {
    private static final String SERVER_URL = "http://192.168.1.13:80/api/v1/";
    private static final String SERVER_RESPONSE_OK = "OK";
    private static final String SERVER_RESPONSE_BAD = "BAD";
    private static final String SERVER_RESPONSE_ERROR = "ERROR";


    private final RequestQueue requestQueue;
    private final ILogger logger;
    private final Context context;

    private int sessionId = -1;
    private String sessionToken = "";

    public static class GameStatus {
        public int remainingTime;
        public int status;
    }

    public static class StatusMessage {
        public StatusMessage(String message) {
            this.message = message;
        }

        public StatusMessage(String message, String rightAnswer) {
            this.message = message;
            this.rightAnswer = rightAnswer;
        }

        public String message;
        public String rightAnswer;
    }


    public Server(Context context, ILogger logger) {
        requestQueue = Volley.newRequestQueue(context);
        this.logger = logger;
        this.context = context;
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
                    callback.onDataReady(false, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
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

    public void login(String login, String password, ServerCallback<Boolean, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("login", login);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/login", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    sessionId = responseData.getInt("session_id");
                    sessionToken = responseData.getString("session_token");
                    logger.logInfo("Server", "Login successful, session id: " + sessionId);
                    if (responseData.has("application_id")) {
                        SharedPreferences.Editor preferences = context.getSharedPreferences(Application.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit();
                        preferences.putInt("application_id", responseData.getInt("application_id"));
                        preferences.putString("application_token", responseData.getString("application_token"));
                        preferences.apply();
                    }
                    ((Application) context.getApplicationContext()).setUserId(responseData.getInt("user_id"));
                    callback.onDataReady(true, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(false, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try login)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (login): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t login " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t login " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try login)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void startFindRoom(ServerCallback<Integer, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
        } catch (JSONException e) {
            return;
        }


        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/get_new_room", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getInt("room_id"), null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(-1, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try get new room)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (get new room): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t get new room " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t get new room " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void loginByApplicationData(String token, int id, ServerCallback<Boolean, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("application_token", token);
            jsonBody.put("application_session_id", id);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/login", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    sessionId = responseData.getInt("session_id");
                    sessionToken = responseData.getString("session_token");
                    logger.logInfo("Server", "Auto Login successful, session id: " + sessionId);
                    if (responseData.has("application_id")) {
                        SharedPreferences.Editor preferences = context.getSharedPreferences(Application.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit();
                        preferences.putInt("application_id", responseData.getInt("application_id"));
                        preferences.putString("application_token", responseData.getString("application_token"));
                        preferences.apply();
                    }
                    ((Application) context.getApplicationContext()).setUserId(responseData.getInt("user_id"));
                    callback.onDataReady(true, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(false, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try Auto login)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (Auto login): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t Auto login " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t login " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try Auto login)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void checkRoom(int roomId, ServerCallback<String, Boolean, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/check_room", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getString("room_status"), true, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(responseData.getString("message"), false, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try check room)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (check room): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t check room " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t check room " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try check room)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void getRole(int roomId, ServerCallback<String, Boolean, Integer> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/get_role", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getString("role"), true, responseData.getInt("painter"));
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(responseData.getString("message"), false, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try get role)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (get role): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t get role " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t get role " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (get role)", null, null);
            }
        });

        requestQueue.add(request);
    }


    public void logout() {
        JSONObject jsonBody = new JSONObject();
        SharedPreferences preferences = context.getSharedPreferences(Application.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        try {
            jsonBody.put("application_session_id", preferences.getInt("application_id", -1));
            jsonBody.put("application_session_token", preferences.getString("application_token", ""));
        } catch (JSONException e) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("application_id");
        editor.remove("application_token");
        editor.apply();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/logout", jsonBody, responseData -> {
        }, error -> {
        });

        requestQueue.add(request);
    }

    public void getMessageForRoom(int roomId, ServerCallback<List<String>, Boolean, String> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/get_messages", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    List<String> resultBuffer = new LinkedList<>();
                    JSONArray dataBuffer = responseData.getJSONArray("messages");
                    for (int i = 0; i < dataBuffer.length(); i++) {
                        resultBuffer.add(dataBuffer.getString(i));
                    }
                    callback.onDataReady(resultBuffer, true, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, false, responseData.getString("message"));
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try get messages)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (get messages): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t get messages " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t get messages " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (get messages)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void getStatusOfRoom(int roomId, ServerCallback<GameStatus, Integer, StatusMessage> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/get_status", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    GameStatus gameStatus = new GameStatus();
                    gameStatus.status = responseData.getInt("game_status");
                    if (responseData.has("remaining_time")) {
                        gameStatus.remainingTime = responseData.getInt("remaining_time");
                    }
                    if (responseData.has("now_painter")) {
                        if (responseData.has("message") && responseData.getString("message").length() > 0) {
                            JSONObject statusMessage = new JSONObject(responseData.getString("message"));
                            if (statusMessage.has("right_answer")) {
                                callback.onDataReady(gameStatus, responseData.getInt("now_painter"), new StatusMessage(statusMessage.getString("message"), statusMessage.getString("right_answer")));
                            } else {
                                callback.onDataReady(gameStatus, responseData.getInt("now_painter"), new StatusMessage(statusMessage.getString("message")));
                            }
                        } else {
                            callback.onDataReady(gameStatus, responseData.getInt("now_painter"), null);
                        }
                    } else {
                        if (responseData.has("message")) {
                            callback.onDataReady(gameStatus, -1, new StatusMessage(responseData.getString("message")));
                        } else {
                            callback.onDataReady(gameStatus, -1, null);
                        }
                    }
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("", -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try get game status)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (get game status): " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t get game status " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t get game status " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (get game status)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void sendVariant(String variant, int roomId, ServerCallback<Boolean, String, Boolean> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
            jsonBody.put("variant", variant);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/try_variant", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getBoolean("result"), null, true);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, responseData.getString("message"), false);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try send variant)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (try send variant): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try send variant " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try send variant " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try send variant)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void sendCanvas(byte[] canvas, int roomId) {
        StringRequest request = new StringRequest(
                Request.Method.POST, SERVER_URL + "game/send_canvas", null, null) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Session-Id", String.valueOf(sessionId));
                params.put("Session-Token", sessionToken);
                params.put("Room-Id", String.valueOf(roomId));
                return params;
            }

            @Override
            public byte[] getBody() {
                return canvas;
            }

            @Override
            public String getBodyContentType() {
                return "image/png";
            }
        };

        requestQueue.add(request);
    }

    public void getCanvas(int roomId, ServerCallback<byte[], Boolean, Object> callback, ServerCallback<String, Integer, Object> errorCallback) {
        StringRequest request = new StringRequest(
                Request.Method.POST, SERVER_URL + "game/get_canvas", responseData -> callback.onDataReady(responseData.getBytes(StandardCharsets.ISO_8859_1), true, null), error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try send variant " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try send variant " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try send variant)", null, null);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("session_id", String.valueOf(sessionId));
                map.put("session_token", sessionToken);
                map.put("room_id", String.valueOf(roomId));
                return map;
            }

            @Override
            public byte[] getBody() {
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("session_id", sessionId);
                    jsonBody.put("session_token", sessionToken);
                    jsonBody.put("room_id", roomId);
                } catch (JSONException e) {
                    return null;
                }

                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        requestQueue.add(request);
    }

    public void getWord(int roomId, ServerCallback<String, Boolean, Object> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/get_word", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getString("word"), true, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(responseData.getString("message"), false, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try get word)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (try get word): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try get word " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try get word " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try get word)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void createPrivateGame(ServerCallback<Integer, String, Boolean> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/create_private_room", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getInt("room_id"), responseData.getString("token"), true);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, responseData.getString("message"), false);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try create private room)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (try create private room): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try create private room " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try create private room " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try create private room)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void getPrivateRoom(String token, ServerCallback<Integer, String, Boolean> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("token", token);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/create_private_room", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(responseData.getInt("room_id"), null, true);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, responseData.getString("message"), false);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try create private room)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (try create private room): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try create private room " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try create private room " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try create private room)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void getUsersPrivateGame(int roomId, ServerCallback<List<String>, String, Boolean> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/get_list_of_private_room", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    List<String> resultBuffer = new LinkedList<>();
                    JSONArray dataBuffer = responseData.getJSONArray("users");
                    for (int i = 0; i < dataBuffer.length(); i++) {
                        resultBuffer.add(dataBuffer.getString(i));
                    }
                    callback.onDataReady(resultBuffer, null, true);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(null, responseData.getString("message"), false);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try create private room)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (try create private room): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try create private room " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try create private room " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try create private room)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void startPrivateGame(int roomId, ServerCallback<String, Boolean, Object> callback, ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
            jsonBody.put("session_token", sessionToken);
            jsonBody.put("room_id", roomId);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "game/start_private_room", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    callback.onDataReady(null, true, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(responseData.getString("message"), false, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try create private room)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (try create private room): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t try create private room " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t try create private room " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try create private room)", null, null);
            }
        });

        requestQueue.add(request);
    }
}

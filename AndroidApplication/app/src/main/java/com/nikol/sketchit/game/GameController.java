package com.nikol.sketchit.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.nikol.sketchit.Application;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class GameController {
    private static final String USER_ROLE = "USER";
    private static final String PAINTER_ROLE = "PAINTER";

    private final int roomId;
    private final Server server;
    private final ILogger logger;
    private final GameLayoutBridge uiBridge;
    private int nowPainter = -1;
    private int userId = -1;

    public GameController(int roomId, GameLayoutBridge uiBridge, Context context) {
        Application application = (Application) context.getApplicationContext();
        this.roomId = roomId;
        this.server = application.getServer();
        this.logger = application.getLogger();
        this.uiBridge = uiBridge;
    }

    public void startGame() {
        uiBridge.setLoadState();
        server.getRole(roomId, new GetRoleServerCallback(), new GetRoleServerErrorCallback());
        logger.logInfo("Game", "Start game");
        userId = uiBridge.getApplication().getUserId();
    }

    public void update() {
//        TODO: Add load image
        if (nowPainter == userId) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            uiBridge.getCanvasImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            server.sendCanvas(byteArray, roomId);
        } else if (nowPainter != -1){
            server.getCanvas(roomId, new ServerCallback<byte[], Boolean, Object>() {
                @Override
                public void onDataReady(byte[] canvas, Boolean arg2, Object arg3) {
                    uiBridge.setCanvas(canvas);
                }
            }, null);
        }

        server.getMessageForRoom(roomId, new ServerCallback<List<String>, Boolean, String>() {
            @Override
            public void onDataReady(List<String> arg1, Boolean arg2, String arg3) {
                if (arg2) {
                    uiBridge.updateChat(arg1);
                    uiBridge.updateRecycleViewPosition();
                } else {
                    logger.logWarming("Game", "Cant get messages for game room");
                }
            }
        }, null);

        server.getStatusOfRoom(roomId, new ServerCallback<Integer, Integer, Boolean>() {
            @Override
            public void onDataReady(Integer gameStatus, Integer nowPainter, Boolean status) {
                serverUpdate(nowPainter, gameStatus);
            }
        }, null);
    }

    private void serverUpdate(int nowPainter, int isNotEnd) {
        if (isNotEnd != 1) {
            exitFromGame();
            return;
        }

        if (nowPainter != this.nowPainter) {
            this.nowPainter = nowPainter;
            if (nowPainter == userId) {
                uiBridge.setPaintState();
            } else {
                uiBridge.setWatchState();
            }
            logger.logInfo("Game", "Update state");
            // TODO: Update now painter
        }
    }

    public void exitFromGame() {
        logger.logInfo("Game", "Exit from game");
        Toast.makeText(uiBridge.getContext(), "Exit from game", Toast.LENGTH_SHORT).show();
        uiBridge.endGame();
    }

    private class GetRoleServerCallback extends ServerCallback<String, Boolean, Integer> {
        @Override
        public void onDataReady(String message, Boolean status, Integer painter) {
            if (status) {
                if (message.equals(USER_ROLE)) {
                    uiBridge.setWatchState();
                    uiBridge.setEnterButtonOnClickListener(view -> {
                        if (!uiBridge.isVariantEmpty()) {
                            logger.logDebug("Game", "Send variant " + uiBridge.getVariant());
                            server.sendVariant(uiBridge.getVariant(), roomId, new ServerCallback<Boolean, String, Boolean>() {
                                @Override
                                public void onDataReady(Boolean result, String message, Boolean status) {
                                    if (status && result) {
//                                        TODO: Win and update state
                                        Toast.makeText(view.getContext(), "You win!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, null);
                        }
                    });
                    nowPainter = painter;
                } else if (message.equals(PAINTER_ROLE)) {
                    uiBridge.setPaintState();
                } else {
                    logger.logInfo("Game", "Can`t reade role " + message);
                    return;
                }
                logger.logInfo("Game", "Get role " + message);
            } else {
                logger.logWarming("Game", "Cant get role " + message);
//                TODO: Add error screen
                // Try again
                server.getRole(roomId, this, new GetRoleServerErrorCallback());
            }
        }
    }

    private class GetRoleServerErrorCallback extends ServerCallback<String, Integer, Object> {
        @Override
        public void onDataReady(String arg1, Integer arg2, Object arg3) {
            logger.logError("Game", "Cant get role " + arg1);
        }
    }
}

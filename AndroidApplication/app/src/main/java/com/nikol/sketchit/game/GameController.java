package com.nikol.sketchit.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.nikol.sketchit.Application;
import com.nikol.sketchit.R;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class GameController {
    private static final String USER_ROLE = "USER";
    private static final String PAINTER_ROLE = "PAINTER";

    private final int roomId;
    private final int userId;
    private final Server server;
    private final ILogger logger;
    private final GameLayoutBridge uiBridge;

    private int nowPainter = -1;
    private boolean isLastAnswerRight = false;

    public GameController(int roomId, GameLayoutBridge uiBridge, Context context) {
        Application application = (Application) context.getApplicationContext();
        this.roomId = roomId;
        this.server = application.getServer();
        this.logger = application.getLogger();
        this.userId = application.getUserId();
        this.uiBridge = uiBridge;

        uiBridge.setEnterButtonOnClickListener(view -> {
            if (!uiBridge.isVariantEmpty()) {
                logger.logInfo("Game", "Send variant " + uiBridge.getVariant());
                server.sendVariant(uiBridge.getVariant(), roomId, new ServerCallback<Boolean, String, Boolean>() {
                    @Override
                    public void onDataReady(Boolean result, String message, Boolean status) {
                        if (status && result) {
                            isLastAnswerRight = true;
                        }
                    }
                }, null);
                uiBridge.clearVariant();
            }
        });
    }

    public void startGame() {
        logger.logInfo("Game", "Start game");
        uiBridge.setLoadState();
        server.getRole(roomId, new GetRoleServerCallback(), new GetRoleServerErrorCallback());
    }

    public void update() {
        if (nowPainter == userId) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            uiBridge.getCanvasImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] canvas = stream.toByteArray();
            server.sendCanvas(canvas, roomId);
        } else {
            server.getCanvas(roomId, new ServerCallback<byte[], Boolean, Object>() {
                @Override
                public void onDataReady(byte[] canvas, Boolean status, Object arg) {
                    uiBridge.setCanvas(canvas);
                }
            }, null);
        }

        server.getMessageForRoom(roomId, new ServerCallback<List<String>, Boolean, String>() {
            @Override
            public void onDataReady(List<String> messages, Boolean status, String arg) {
                if (status) {
                    uiBridge.updateChat(messages);
                    uiBridge.updateRecycleViewPosition();
                } else {
                    logger.logWarming("Game", "Cant get messages for game room");
                }
            }
        }, null);

        server.getStatusOfRoom(roomId, new ServerCallback<Server.GameStatus, Integer, Server.StatusMessage>() {
            @Override
            public void onDataReady(Server.GameStatus gameStatus, Integer nowPainter, Server.StatusMessage message) {
                serverUpdate(nowPainter, gameStatus.status, message, gameStatus.remainingTime);
            }
        }, null);
    }

    private void serverUpdate(int nowPainter, int isNotEnd, Server.StatusMessage message, int remainingTime) {
        if (isNotEnd != 1 && isNotEnd != 2) {
            exitFromGame();
            return;
        }

        if (isNotEnd == 1) {
            // TODO: Added check for now state
            if (!uiBridge.isMessageState()) {
                uiBridge.setMessageState(message.message, message.rightAnswer, isLastAnswerRight, remainingTime);
                isLastAnswerRight = false;
            }
            return;
        }

        uiBridge.setRemainingTime(remainingTime);

        if (nowPainter != this.nowPainter) {
            isLastAnswerRight = false;
            this.nowPainter = nowPainter;
            if (nowPainter == userId) {
                uiBridge.setPaintState();
                updateWord();
            } else {
                uiBridge.setWatchState();
            }
            logger.logInfo("Game", "Update state");
        }
    }

    public void exitFromGame() {
        logger.logInfo("Game", "Exit from game");
        Toast.makeText(uiBridge.getContext(), R.string.message_game_end, Toast.LENGTH_SHORT).show();
        uiBridge.endGame();
    }

    private void updateWord() {
        server.getWord(roomId, new ServerCallback<String, Boolean, Object>() {
            @Override
            public void onDataReady(String message, Boolean status, Object arg) {
                if (status) {
                    logger.logInfo("Game", "Get word " + message);
                    uiBridge.setWord(message);
                } else {
                    logger.logError("Game", "Can`t get word " + message);
                }
            }
        }, new ServerCallback<String, Integer, Object>() {
            @Override
            public void onDataReady(String message, Integer errorCode, Object arg3) {
                logger.logError("Game", "Can`t get word " + message);
            }
        });
    }

    private class GetRoleServerCallback extends ServerCallback<String, Boolean, Integer> {
        @Override
        public void onDataReady(String message, Boolean status, Integer painter) {
            if (status) {
                if (message.equals(USER_ROLE)) {
                    uiBridge.setWatchState();
                    nowPainter = painter;
                } else if (message.equals(PAINTER_ROLE)) {
                    uiBridge.setPaintState();
                    updateWord();
                } else {
                    logger.logInfo("Game", "Can`t read role " + message);
                    return;
                }
                logger.logInfo("Game", "Get role " + message);
            } else {
                logger.logWarming("Game", "Cant get role " + message);
                // Try again
                server.getRole(roomId, this, new GetRoleServerErrorCallback());
            }
        }
    }

    private class GetRoleServerErrorCallback extends ServerCallback<String, Integer, Object> {
        @Override
        public void onDataReady(String message, Integer errorCode, Object arg) {
            logger.logError("Game", "Cant get role " + message);
        }
    }
}

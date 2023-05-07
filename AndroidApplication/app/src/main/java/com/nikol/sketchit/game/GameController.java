package com.nikol.sketchit.game;

import android.content.Context;

import com.nikol.sketchit.Application;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;
import com.nikol.sketchit.server.ServerCallback;

public class GameController {
    private static final String USER_ROLE = "USER";
    private static final String PAINTER_ROLE = "PAINTER";

    private enum Role {
        USER,
        DRAWER
    }

    private enum State {
        WAITING,
        PAINTER,
        WATCHING,
        FINISHED
    }

    private final int roomId;
    private final Server server;
    private final ILogger logger;
    private final GameLayoutBridge uiBridge;
    private Role nowRole = null;
    private State nowState = State.WAITING;

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
    }

    public void update() {

    }

    public void exitFromGame() {
        logger.logInfo("Game", "Exit from game");
    }

    private class GetRoleServerCallback extends ServerCallback<String, Boolean, Object> {
        @Override
        public void onDataReady(String arg1, Boolean arg2, Object arg3) {
            if (arg2) {
                if(arg1.equals(USER_ROLE)) {
                    nowRole = Role.USER;
                    nowState = State.WATCHING;
                    uiBridge.setWatchState();
                }else if(arg1.equals(PAINTER_ROLE)) {
                    nowRole = Role.DRAWER;
                    nowState = State.PAINTER;
                    uiBridge.setPaintState();
                }else {
                    logger.logInfo("Game", "Can`t reade role " + arg1);
                    return;
                }
                logger.logInfo("Game", "Get role " + arg1);
            } else {
                logger.logWarming("Game", "Cant get role " + arg1);
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

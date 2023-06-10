package com.nikol.sketchit;

import com.nikol.sketchit.loggers.ConsoleLogger;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;

public class Application extends android.app.Application {
    public static final String PREFERENCES_FILE_NAME = "preferences";
    public static final int ROUND_SECONDS_TIME = 210;
    public static final int WAITING_SECONDS_TIME = 10;

    private final ILogger logger;
    private Server server;
    private int userId;

    public Application() {
        logger = new ConsoleLogger();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        server = new Server(this, logger);
    }

    public Server getServer() {
        return server;
    }

    public ILogger getLogger() {
        return logger;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

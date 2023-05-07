package com.nikol.sketchit;

import com.nikol.sketchit.loggers.ConsoleLogger;
import com.nikol.sketchit.loggers.ILogger;
import com.nikol.sketchit.server.Server;

public class Application extends android.app.Application {
    public static final String PREFERENCES_FILE_NAME = "preferences";

    private Server server;
    private final ILogger logger;

    public Application(){
        logger = new ConsoleLogger();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        server = new Server(this, logger);
    }

    public Server getServer(){
        return server;
    }

    public ILogger getLogger(){
        return logger;
    }
}

package com.example.itogoviyproject;

import com.example.itogoviyproject.loggers.ConsoleLogger;
import com.example.itogoviyproject.loggers.ILogger;
import com.example.itogoviyproject.server.Server;

public class Application  extends android.app.Application {
    private Server server;
    private ILogger logger;

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

package com.example.itogoviyproject.loggers;

public interface ILogger {
    public void logInfo(String tag, String message);
    public void logWarming(String tag, String message);
    public void logError(String tag, String message);
}

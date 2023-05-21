package com.nikol.sketchit.loggers;

/*
This class abstracts the general interface for the logging class. This is necessary because
different types of logging can be useful in different situations. The usual console for debugging
the program or sending to the server for further analysis and processing
 */
public interface ILogger {
    void logDebug(String tag, String message);

    void logInfo(String tag, String message);

    void logWarming(String tag, String message);

    void logError(String tag, String message);
}

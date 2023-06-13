package com.nikol.sketchit.loggers;

import android.util.Log;

/**
 * Простейшая реализация системы логирования для вывода в консоль.
 */
public class ConsoleLogger implements ILogger {
    @Override
    public void logDebug(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void logInfo(String tag, String message) {
        Log.i(tag, message);
    }

    @Override
    public void logWarming(String tag, String message) {
        Log.w(tag, message);
    }

    @Override
    public void logError(String tag, String message) {
        Log.e(tag, message);
    }
}
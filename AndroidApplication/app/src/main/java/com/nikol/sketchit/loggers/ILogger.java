package com.nikol.sketchit.loggers;

/**
 * Этот класс абстрагирует общий интерфейс для класса ведения журнала. Это необходимо, поскольку
 * различные типы ведения журнала могут быть полезны в разных ситуациях. Обычная консоль для отладки
 * программы или отправки на сервер для дальнейшего анализа и обработки.
 */
public interface ILogger {
    void logDebug(String tag, String message);

    void logInfo(String tag, String message);

    void logWarming(String tag, String message);

    void logError(String tag, String message);
}

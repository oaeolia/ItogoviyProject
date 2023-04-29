CREATE TABLE `users`
(
    `id`       INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT,
    `name`     VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `email`    VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `password` VARCHAR(65) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `sessions`
(
    `id`        INT UNSIGNED                                                   NOT NULL AUTO_INCREMENT,
    `user_id`   INT UNSIGNED                                                   NOT NULL,
    `token`     VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NOT NULL,
    `data`      VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `last_time` DATETIME                                                       NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

CREATE TABLE `application_sessions`
(
    `id`        INT UNSIGNED                                                   NOT NULL AUTO_INCREMENT,
    `user_id`   INT UNSIGNED                                                   NOT NULL,
    `token`     VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `last_time` DATETIME                                                       NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

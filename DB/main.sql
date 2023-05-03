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

CREATE TABLE `games_rooms`
(
    `id`                 INT UNSIGNED                                                   NOT NULL AUTO_INCREMENT,
    `now_word`           VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `is_started`         BOOLEAN                                                        NOT NULL DEFAULT 0,
    `user_1`             INT UNSIGNED,
    `user_2`             INT UNSIGNED,
    `user_3`             INT UNSIGNED,
    `user_4`             INT UNSIGNED,
    `user_5`             INT UNSIGNED,
    `checked_user_1`     boolean                                                        NOT NULL DEFAULT 0,
    `checked_user_2`     boolean                                                        NOT NULL DEFAULT 0,
    `checked_user_3`     boolean                                                        NOT NULL DEFAULT 0,
    `checked_user_4`     boolean                                                        NOT NULL DEFAULT 0,
    `checked_user_5`     boolean                                                        NOT NULL DEFAULT 0,
    `start_checked_time` DATETIME,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_1`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_2`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_3`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_4`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_5`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

CREATE TABLE `words`
(
    `id`       INT UNSIGNED                                                   NOT NULL AUTO_INCREMENT,
    `word` VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`id`)
)

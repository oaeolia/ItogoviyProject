CREATE TABLE `samsung_finally_project`.`users`
(
    `id`       INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT,
    `name`     VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    `email`    VARCHAR(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `password` VARCHAR(65) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL,
    PRIMARY KEY (`id`)
);
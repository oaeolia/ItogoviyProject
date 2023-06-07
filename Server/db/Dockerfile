FROM mysql:8

ENV MYSQL_DATABASE=main.sql \
    MYSQL_ROOT_PASSWORD=root \
    MYSQL_ALLOW_EMPTY_PASSWORD=yes

ADD main.sql /docker-entrypoint-initdb.d

EXPOSE 3306
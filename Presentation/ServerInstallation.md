# Инструкция по развертыванию сервера
Весь исходный код проекта можно найти по этой [ссылке](https://github.com/oaeolia/SketchIt) на GitHub
Ниже приведена инструкция по установке сервера на операционную систему Ubuntu.

# Установка Ubuntu на Oracle Virtual Box (64 битной)
Для начала проверьте системные требования! Ниже приведен процесс только для 64 битных устройств.
Создание виртуальной машины:
1. Скачиваем и устанавливаем Virtual Box (если отключена, то включаем виртуализацию в биосе);
2. Открываем Virtual Box;
3. Нажимаем кнопку `Cоздать`;
4. Указываем имя машины папку для хранения данных по выбору;
5. Тип указываем `linux`, версия - `Ubuntu (64-bit)`
6. Нажимаем кнопку `Далее`;
7. Указываем выделенный объем оперативной памяти (минимум 3 GB);
8. Нажимаем кнопку `Далее`;
9. Выбираем пункт `Создать новый виртуальный жесткий диск`;
10. Нажимаем кнопку `Создать`;
11. Выбираем пункт `VDI (VirtualBox Disk Image)`;
12. Нажимаем кнопку `Далее`;
13. Выбираем пункт `Фиксированный виртуальный жесткий диск`;
14. Нажимаем кнопку `Далее`;
15. Указываем размер виртуального диска (минимум 15 GB) и место хранения;
16. Нажимаем кнопку `Создать`.
Настройка сети:
1. Открываем настройки виртуальной машины (выбрать машину, кнопка `Настроить`);
2. Слева выбираем пункт `Сеть`;
3. Вкладка `Адаптер 1`;
4. Ставим тип подключения `Сетевой мост`;
5. Нажимаем кнопку `Ок`.
Установка Ubuntu на виртуальную машину:
1. Скачиваем с официального сайта iso образ Ubuntu 22.04.2 LTS (https://releases.ubuntu.com/22.04/ubuntu-22.04.2-live-server-amd64.iso);
2. Запускаем созданную виртуальную машину;
3. Ждем пока появиться всплывающее окно `Select start-up disk`;
4. Выбираем скачанный iso образ;
5. Нажимаем кнопку `Запустить`;
6. Дальше следуем процессу установки.


# Установка зависимостей
``` bash
sudo apt update
sudo apt install nginx
sudo apt install python3.9
sudo apt install mysql-server mysql-client
```


# Установка сервера 
Для начала требуется создать папку проекта. Пусть она будет расположена по пути /home/{username}/sketch_it:
``` bash
cd /home/{user_name}
mkdir sketch_it
```

Затем скачаем туда проект:
``` bash
cd /home/{user_name}
git clone https://github.com/oaeolia/SketchIt.git sketch_it
```
Создаем virtual venv для python:
``` bash
cd /home/{user_name}/skect_it/Server
python3 -m venv venv
```
Устанавливаем зависимости проекта:
``` bash
cd /home/{user_name}/skect_it/Server
source venv/bin/activate
pip install -r requirements.txt
pip install gunicorn
deactivate
```

# Инициализация базы данных
Для начала создадим базу данных. При установке mysql возможно вам потребовалось указать имя супер пользователя и его пароль. В этом случае меняйте root на свое имя пользователя и после -p добавляем свой пароль. Если вы не устанавливали пароль, то в точности действуйте по инструкции, при требовании пароля любая комбинация проходит.

``` bash
sudo mysql -u root -p
```
Дальше создадим базу данных и импортируем таблицы:
``` mysql
CREATE DATABASE sketch_it;
USE sketch_it
SOURCE /home/{username}/sketch_it/DB/main.sql;
```
Теперь создадим пользователя для системы и дадим ему права на редактирование:
``` mysql
CREATE USER 'sketch_it_system'@'localhost' IDENTIFIED BY 'ske1ch_pass_!t_system_password';
GRANT DELETE, UPDATE, INSERT ON sketch_it . * TO 'sketch_it_system'@'localhost';
```
Завершим работу c mysql:
``` mysql
exit
```

# Внос параметров конфигурации
Создадим файл конфигурации сервера:
``` bash
cd /home/{user_name}/skect_it/Server/venv
sudo vim config.env
```
Вставим в файл следующие данных:
```
DB_NAME = sketch_it
DB_USER_NAME = sketch_it_system
DB_PASSWORD = ske1ch_pass_!t_system_password
```
## Вставка данных в vim
Нажимаем `i`;
Вводим текст;
Нажимаем `ESC`;
Вводим `:wqa`;
Нажимаем enter.

# Создание службы 
Для того, что бы сервер работал в фоновом режиме создадим службу:
``` bash
cd /etc/systemd/system
sudo vim sketch_it.service
```
Вставляем следующий текст, если надо заменяя параметр --workers (кол-во потоков исполнения):
``` 
[Unit]
Description=Gunicorn instance to server sketch it
After=multi-user.target
[Service]
Type=simple
Working Directory=/home/{username}/sketch_it/Server
ExecStart=/home/{username}/sketch_it/Server/venv/bin/python -m gunicorn --workers 4 --bind unix:sketch_it.sock main: app
Environment="/home/{username}/sketch_it/Server/venv/bin/activate"
Restart=always
[Install]
WantedBy=multi-user.target
```
Сохраняем и запускаем службу:
``` bash
sudo systemctl daemon-reload
sudo systemctl start sketch_it.service
sudo systemctl enable sketch_it.service
```

# Настройка nginx
Создадим конфигурации:
``` bash
cd /etc/nginx/sites-available
sudo vim sketch_it
```
Вставим в него следящие данные:
``` nginx
server {
    listen 80;
    server_name {domain};

    location / {
        include proxy_params;
        proxy_pass http://unix:/home/{username}/sketch_it/Server/sketch_it.sock;
    }
}

```
Вместо `{domain}` надо вставить или домен (например `myproject.ru` и `www.myproject`), или локальный ip адрес в сети (например `192.168.1.2`, найти его можно выполнив команду `ifconfig`, предварительно установив net-tools, при помощи команды `sudo apt install net-tools`)
Сохраняем и перезагружаем nginx:
``` bash
sudo systemctl restart nginx
```

# Заключение
Для того, что бы приложения знало адрес сервер, необходимо в проект Android Studio внести правку: в классе Server (пакет com.nikol.sketchit.server), строку 27 изменить на (вставить домен или ip адрес сервера на место `{domain}`):
``` java
	private static final String SERVER_URL = "http://{domain}/api/v1/";
```
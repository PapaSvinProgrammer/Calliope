# 🚀 Деплой Mordva Radio Backend на LAMP-сервер в Yandex Cloud

## Обзор архитектуры

```
Клиент ──HTTPS:443──► Apache (LAMP VM) ──HTTP:8080──► Spring Boot (Docker)
                           │                                    │
                     TLS-сертификат                    PostgreSQL :5432
                  (Yandex Cloud Certificate Manager)    (LAMP VM)
```

Spring Boot работает в Docker-контейнере с `network_mode: host` — контейнер обращается к PostgreSQL на `localhost:5432` напрямую. Apache принимает HTTPS-запросы и проксирует их на порт `8080`.

---

## Требования к VM

| Ресурс  | Минимум                                        |
|---------|------------------------------------------------|
| OS      | Ubuntu 20.04+ / Debian 11+                     |
| CPU     | 1 ядро                                         |
| RAM     | 2 ГБ                                           |
| Disk    | 5 ГБ                                           |
| Apache  | 2.4+ (уже установлен в LAMP)                   |
| Docker  | 24+                                            |
| Compose | v2+                                            |

> **PostgreSQL** нужно установить отдельно — стандартный LAMP включает MySQL/MariaDB, но приложение работает с PostgreSQL.

**Предварительные требования для Certificate Manager:**
- Публичное доменное имя, чья A-запись указывает на публичный IP вашей VM
- Установленный `yc` CLI (Yandex Cloud CLI)
- Установленный `jq`

---

## Шаг 1 — Установка PostgreSQL

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib -y

sudo systemctl enable --now postgresql
sudo systemctl status postgresql
```

### Создание базы данных и пользователя

```bash
sudo -u postgres psql << 'EOF'
CREATE USER radio WITH PASSWORD 'ваш_надёжный_пароль';
CREATE DATABASE radio OWNER radio;
GRANT ALL PRIVILEGES ON DATABASE radio TO radio;
\q
EOF
```

---

## Шаг 2 — Установка Docker

```bash
sudo apt update && sudo apt upgrade -y
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker

# Проверяем
docker --version
docker compose version
```

---

## Шаг 3 — Копирование проекта на VM

**Через Git:**
```bash
git clone <URL_РЕПОЗИТОРИЯ> radio-backend
cd radio-backend
```

**Или через scp (с локальной машины):**
```bash
scp -r ./backend_radio user@<IP_VM>:~/radio-backend
```

---

## Шаг 4 — Настройка переменных окружения

```bash
cd ~/radio-backend
cp .env.example .env
nano .env
```

Заполните файл `.env`:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/radio
SPRING_DATASOURCE_USERNAME=radio
SPRING_DATASOURCE_PASSWORD=ваш_надёжный_пароль
```

> Контейнер использует `network_mode: host`, поэтому `localhost` внутри контейнера = `localhost` VM.

---

## Шаг 5 — Запуск Spring Boot

```bash
docker compose up -d --build
```

При первом запуске Gradle скачает зависимости и соберёт JAR (~2–5 мин). Следите за прогрессом:

```bash
docker compose logs -f app
```

Успешный старт:
```
Started DemoApplication in 4.312 seconds
```

Проверьте локально:
```bash
curl http://localhost:8080/api/city
```

---

## Шаг 6 — Настройка Apache как reverse proxy

### Включение необходимых модулей

```bash
sudo a2enmod proxy proxy_http ssl headers rewrite
sudo systemctl restart apache2
```

### Виртуальный хост для HTTP (временный, для прохождения ACME-challenge)

Создайте `/etc/apache2/sites-available/radio-backend.conf`:

```apache
<VirtualHost *:80>
    ServerName your-domain.com

    # Директория для ACME-challenge (Yandex Cloud Certificate Manager)
    Alias /.well-known/acme-challenge/ /var/www/html/.well-known/acme-challenge/
    <Directory "/var/www/html/.well-known/acme-challenge/">
        Options None
        AllowOverride None
        Require all granted
    </Directory>

    # Проксирование на Spring Boot
    ProxyPreserveHost On
    ProxyPass        /.well-known/acme-challenge/ !
    ProxyPass        / http://127.0.0.1:8080/
    ProxyPassReverse / http://127.0.0.1:8080/

    RequestHeader set X-Forwarded-Proto "http"

    ErrorLog  ${APACHE_LOG_DIR}/radio-backend-error.log
    CustomLog ${APACHE_LOG_DIR}/radio-backend-access.log combined
</VirtualHost>
```

> ⚠️ Замените `your-domain.com` на ваш реальный домен.

```bash
sudo a2ensite radio-backend.conf
sudo a2dissite 000-default.conf   # опционально
sudo apache2ctl configtest
sudo systemctl reload apache2
```

---

## Шаг 7 — Получение HTTPS-сертификата через Yandex Cloud Certificate Manager

### 7.1. Установка инструментов

> **Если `apt` выдаёт `Could not get lock /var/lib/dpkg/lock-frontend`** — дождитесь завершения фонового обновления:
> ```bash
> sudo systemctl stop unattended-upgrades
> ```

```bash
# jq для парсинга JSON
sudo apt install jq -y

# Yandex Cloud CLI
curl -sSL https://storage.yandexcloud.net/yandexcloud-yc/install.sh | bash
source ~/.bashrc

# Проверить установку
yc --version
```

### 7.1а. Авторизация yc CLI на headless VM (без браузера)

Обычный `yc init` пытается открыть браузер, что не работает на сервере. Используйте OAuth-токен:

**На локальной машине** (с браузером) получите токен по ссылке:
```
https://oauth.yandex.ru/authorize?response_type=token&client_id=1a6990aa636648e9b2ef855fa7bec2fb
```

**На VM** настройте `yc` с токеном без интерактивного мастера:

```bash
# Подставьте ваши реальные значения
YC_TOKEN="y0_AgAAAA..."        # OAuth-токен с локальной машины
YC_CLOUD_ID="b1g..."           # ID облака: yc resource-manager cloud list
YC_FOLDER_ID="b1g..."          # ID каталога: yc resource-manager folder list

yc config set token        "$YC_TOKEN"
yc config set cloud-id     "$YC_CLOUD_ID"
yc config set folder-id    "$YC_FOLDER_ID"

# Проверка
yc config list
```

### 7.2. Создание Let's Encrypt сертификата

```bash
yc certificate-manager certificate create \
  --name radio-backend-cert \
  --domains "your-domain.com" \
  --type letsencrypt \
  --description "TLS cert for Mordva Radio Backend"
```

Сохраните ID сертификата:

```bash
CERT_ID=$(yc certificate-manager certificate list --format json | \
  jq -r '.[] | select(.name=="radio-backend-cert") | .id')
echo "CERT_ID=$CERT_ID"
```

### 7.3. Прохождение HTTP-01 challenge

Получите данные для проверки домена:

```bash
yc certificate-manager certificate get --id "$CERT_ID" --format json | \
  jq '.challenges[]'
```

Вы получите объект вида:
```json
{
  "type": "HTTP",
  "status": "PENDING",
  "path": "/.well-known/acme-challenge/<TOKEN>",
  "content": "<KEY_AUTHORIZATION>"
}
```

Создайте файл проверки:

```bash
sudo mkdir -p /var/www/html/.well-known/acme-challenge
sudo chown -R www-data:www-data /var/www/html/.well-known
sudo chmod -R 755 /var/www/html/.well-known

# Подставьте реальные значения TOKEN и KEY_AUTHORIZATION из команды выше
echo "<KEY_AUTHORIZATION>" | \
  sudo tee /var/www/html/.well-known/acme-challenge/<TOKEN>
```

Проверьте, что файл доступен:

```bash
curl http://your-domain.com/.well-known/acme-challenge/<TOKEN>
# Ожидается: вывод KEY_AUTHORIZATION
```

### 7.4. Ожидание выпуска сертификата

Статус изменится с `VALIDATING` → `ISSUED` за 5–15 минут:

```bash
watch -n 30 "yc certificate-manager certificate get --id \"$CERT_ID\" --format json | \
  jq '{status: .status, not_after: .not_after}'"
```

### 7.5. Скачивание сертификата на VM

> Флаги `--chain` и `--key` принимают путь к файлу как аргумент. Скачиваем во временную папку, затем переносим через `sudo`.

```bash
sudo mkdir -p /etc/apache2/ssl/radio-backend

# Скачать цепочку и ключ во временную папку
yc certificate-manager certificate content \
  --id "$CERT_ID" \
  --chain ~/fullchain.pem \
  --key ~/privkey.pem

# Переместить в /etc/apache2/ с правами root
sudo mv ~/fullchain.pem /etc/apache2/ssl/radio-backend/fullchain.pem
sudo mv ~/privkey.pem   /etc/apache2/ssl/radio-backend/privkey.pem

# Установите права доступа
sudo chmod 644 /etc/apache2/ssl/radio-backend/fullchain.pem
sudo chmod 600 /etc/apache2/ssl/radio-backend/privkey.pem

# Проверьте
ls -la /etc/apache2/ssl/radio-backend/
```

---

## Шаг 8 — Настройка Apache для HTTPS

Обновите `/etc/apache2/sites-available/radio-backend.conf`:

```apache
# HTTP → HTTPS редирект
<VirtualHost *:80>
    ServerName your-domain.com

    # ACME-challenge (для обновления сертификата)
    Alias /.well-known/acme-challenge/ /var/www/html/.well-known/acme-challenge/
    <Directory "/var/www/html/.well-known/acme-challenge/">
        Options None
        AllowOverride None
        Require all granted
    </Directory>

    # Редирект всего остального на HTTPS
    RewriteEngine On
    RewriteCond %{REQUEST_URI} !^/\.well-known/acme-challenge/
    RewriteRule ^ https://%{HTTP_HOST}%{REQUEST_URI} [END,NE,R=permanent]
</VirtualHost>

# HTTPS
<VirtualHost *:443>
    ServerName your-domain.com

    # Сертификат из Yandex Cloud Certificate Manager
    SSLEngine on
    SSLCertificateFile    /etc/apache2/ssl/radio-backend/fullchain.pem
    SSLCertificateKeyFile /etc/apache2/ssl/radio-backend/privkey.pem

    # Рекомендуемые параметры TLS (Mozilla Modern)
    SSLProtocol             all -SSLv3 -TLSv1 -TLSv1.1
    SSLCipherSuite          ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384
    SSLHonorCipherOrder     off

    # HSTS — браузер всегда использует HTTPS
    Header always set Strict-Transport-Security "max-age=63072000"

    # Reverse proxy → Spring Boot
    ProxyPreserveHost On
    ProxyPass        / http://127.0.0.1:8080/
    ProxyPassReverse / http://127.0.0.1:8080/

    RequestHeader set X-Forwarded-Proto "https"
    RequestHeader set X-Real-IP         %{REMOTE_ADDR}s

    ErrorLog  ${APACHE_LOG_DIR}/radio-backend-ssl-error.log
    CustomLog ${APACHE_LOG_DIR}/radio-backend-ssl-access.log combined
</VirtualHost>
```

Примените:

```bash
sudo apache2ctl configtest
sudo systemctl reload apache2
```

---

## Шаг 9 — Открытие портов в Yandex Cloud

### Файрвол на VM

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload
sudo ufw status
```

### Security Group в Yandex Cloud Console

В веб-консоли Yandex Cloud → **VPC** → **Security Groups** → группа вашей VM:

| Направление | Протокол | Порт | Источник     |
|-------------|----------|------|--------------|
| Входящий    | TCP      | 80   | 0.0.0.0/0    |
| Входящий    | TCP      | 443  | 0.0.0.0/0    |

---

## Шаг 10 — Проверка HTTPS

```bash
# Проверьте HTTPS
curl https://your-domain.com/api/city

# Проверьте редирект HTTP → HTTPS
curl -I http://your-domain.com/api/city
# Ожидается: 301 Moved Permanently → https://...

# Проверьте сертификат
echo | openssl s_client -connect your-domain.com:443 -servername your-domain.com 2>/dev/null | \
  openssl x509 -noout -dates -subject
```

---

## Обновление сертификата (раз в ~90 дней)

Yandex Cloud Certificate Manager **автоматически обновляет** Let's Encrypt сертификат за ~30 дней до истечения срока. Однако файлы на VM нужно перекачивать вручную.

Срок действия сертификата можно проверить:
```bash
echo | openssl s_client -connect mordva-calliope.ru:443 -servername mordva-calliope.ru 2>/dev/null | \
  openssl x509 -noout -dates
# notAfter — дата истечения
```

### Обновление через локальную машину

Выполните **на локальной машине** (где работает `yc`):

```bash
CERT_ID="fpq014rfs5rhu0c11pfc"
VM_USER="yc-user"
VM_HOST="89.169.182.128"

# Скачать обновлённый сертификат
yc certificate-manager certificate content \
  --id "$CERT_ID" \
  --chain ~/fullchain.pem \
  --key ~/privkey.pem

# Скопировать на VM
scp ~/fullchain.pem ${VM_USER}@${VM_HOST}:~/fullchain.pem
scp ~/privkey.pem   ${VM_USER}@${VM_HOST}:~/privkey.pem

# Переместить в нужную директорию и перезагрузить Apache
ssh ${VM_USER}@${VM_HOST} "
  sudo mv ~/fullchain.pem /etc/apache2/ssl/radio-backend/fullchain.pem && \
  sudo mv ~/privkey.pem   /etc/apache2/ssl/radio-backend/privkey.pem && \
  sudo chmod 644 /etc/apache2/ssl/radio-backend/fullchain.pem && \
  sudo chmod 600 /etc/apache2/ssl/radio-backend/privkey.pem && \
  sudo apache2ctl configtest && sudo systemctl reload apache2 && \
  echo 'Сертификат обновлён!'
"

# Удалите локальные копии
rm ~/fullchain.pem ~/privkey.pem
```

> **Когда обновлять:** установите напоминание за 2 недели до даты `notAfter`. Обычно это раз в 3 месяца.

---

## Доступные API эндпоинты

| Метод | URL                                   | Описание                           |
|-------|---------------------------------------|------------------------------------|
| GET   | `/api/city`                           | Список городов (`?page=0&size=20`) |
| GET   | `/api/city/{id}/radio-stations`       | Радиостанции города                |
| GET   | `/api/city/search?name=...`           | Поиск городов по названию          |
| GET   | `/api/radio-stations`                 | Все радиостанции                   |
| GET   | `/api/radio-stations/{id}`            | Радиостанция по ID                 |
| GET   | `/api/radio-stations/search?name=...` | Поиск радиостанций по названию     |

---

## Управление контейнером

```bash
docker compose ps              # статус
docker compose logs -f app     # логи
docker compose down            # остановить
docker compose restart app     # перезапустить без пересборки
docker compose up -d --build   # пересобрать после изменений кода
```

## Управление Apache

```bash
sudo apache2ctl configtest     # проверить конфигурацию
sudo systemctl reload apache2  # перезагрузить конфиг
sudo systemctl status apache2  # статус
```

---

## Устранение неполадок

| Проблема | Решение |
|----------|---------|
| `Connection refused` на порту 8080 | `docker compose ps` — контейнер должен быть `Up`. Логи: `docker compose logs app` |
| `502 Bad Gateway` в Apache | Spring Boot не запущен или упал. `docker compose logs -f app` |
| PostgreSQL недоступен | `sudo systemctl status postgresql`. Проверьте credentials в `.env` |
| Сертификат застрял в `VALIDATING` | Проверьте доступность challenge: `curl http://your-domain.com/.well-known/acme-challenge/<TOKEN>`. A-запись домена должна вести на публичный IP VM |
| `ERR_SSL_PROTOCOL_ERROR` | Порт 443 закрыт в UFW или Security Group Yandex Cloud |
| `SSL: key values mismatch` в Apache | Несоответствие ключа и сертификата — перескачайте оба файла заново |
| Сертификат истёк | Проверьте лог: `cat /var/log/update-radio-cert.log`. Запустите скрипт вручную: `sudo /usr/local/bin/update-radio-cert.sh` |
| Apache: `AH00526: Premature end of script` | `sudo apache2ctl configtest` — исправьте синтаксическую ошибку в конфиге |

---

## Структура файлов проекта

```
backend_radio/
├── Dockerfile              # Многоэтапная сборка (Gradle → JRE 21)
├── docker-compose.yml      # Запуск Spring Boot (network_mode: host)
├── .dockerignore           # Исключения для Docker-контекста
├── .env.example            # Шаблон переменных окружения
└── DEPLOY.md               # Эта инструкция
```

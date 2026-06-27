# 🚀 Деплой Mordva Radio Backend на виртуальную машину

## Требования к VM

| Ресурс   | Минимум      |
|----------|-------------|
| OS       | Ubuntu 20.04+ / Debian 11+ |
| CPU      | 1 ядро      |
| RAM      | 2 ГБ        |
| Disk     | 5 ГБ        |
| Docker   | 24+         |
| Compose  | v2+         |

> **База данных** уже запущена на VM (localhost:5432). Docker запускает только Spring Boot приложение.

---

## Быстрый старт

```bash
# 1. Клонируйте репозиторий на VM
git clone <URL_РЕПОЗИТОРИЯ> radio-backend
cd radio-backend

# 2. Создайте .env из шаблона и задайте credentials для БД
cp .env.example .env
nano .env

# 3. Запустите
docker compose up -d --build

# 4. Готово! Бекенд доступен по адресу:
# http://<IP_МАШИНЫ>:8080/api/city
```

---

## Пошаговая инструкция

### Шаг 1 — Установка Docker

Если на VM ещё нет Docker:

```bash
sudo apt update && sudo apt upgrade -y
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker

# Проверяем
docker --version
docker compose version
```

### Шаг 2 — Копирование проекта на VM

**Вариант A — через Git:**
```bash
git clone <URL_РЕПОЗИТОРИЯ> radio-backend
cd radio-backend
```

**Вариант B — через scp:**
```bash
# На локальной машине:
scp -r ./backend_radio user@<IP_VM>:~/radio-backend

# На VM:
cd ~/radio-backend
```

### Шаг 3 — Настройка подключения к БД

```bash
cp .env.example .env
nano .env
```

Содержимое `.env`:
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/radio
SPRING_DATASOURCE_USERNAME=radio
SPRING_DATASOURCE_PASSWORD=ваш_пароль_от_БД
```

> Приложение запускается в Docker с `network_mode: host`, поэтому `localhost` внутри контейнера = `localhost` VM, и подключение к PostgreSQL работает напрямую.

### Шаг 4 — Запуск

```bash
docker compose up -d --build
```

При первом запуске Gradle скачает зависимости и соберёт JAR (~2-5 мин).

### Шаг 5 — Проверка логов

```bash
# Логи приложения
docker compose logs -f app
```

---

## Проверка работы

```bash
# Список городов
curl http://<IP_VM>:8080/api/city

# Все радиостанции
curl http://<IP_VM>:8080/api/radio-stations

# Поиск городов
curl "http://<IP_VM>:8080/api/city/search?name=Саранск"
```

### Доступные эндпоинты

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/city` | Список городов (пагинация: `?page=0&size=20`) |
| GET | `/api/city/{id}/radio-stations` | Радиостанции города |
| GET | `/api/city/search?name=...` | Поиск городов по названию |
| GET | `/api/radio-stations` | Все радиостанции |
| GET | `/api/radio-stations/{id}` | Радиостанция по ID |
| GET | `/api/radio-stations/search?name=...` | Поиск радиостанций |

---

## Управление

```bash
# Остановить контейнер
docker compose down

# Пересобрать после изменений в коде
docker compose up -d --build

# Статус контейнера
docker compose ps

# Рестарт приложения (без пересборки)
docker compose restart app
```

---

## Открытие портов

Если бекенд недоступен извне, убедитесь что порт 8080 открыт в файрволе:

```bash
# UFW (Ubuntu)
sudo ufw allow 8080/tcp

# iptables
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
```

Также проверьте **security group / firewall** в панели облачного провайдера — порт 8080 должен быть открыт для входящих TCP-соединений.

---

## Настройка Nginx (опционально)

Если вы хотите, чтобы бекенд был доступен на 80-м порту или по домену:

```bash
sudo apt install nginx -y
```

Создайте конфиг `/etc/nginx/sites-available/radio-backend`:

```nginx
server {
    listen 80;
    server_name your-domain.com;  # или IP машины

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/radio-backend /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

После этого бекенд будет доступен по `http://<IP_VM>/api/city` (без порта).

---

## 🔒 Настройка HTTPS через Yandex Cloud

В этом разделе описан полный процесс получения бесплатного TLS-сертификата через **Yandex Cloud Certificate Manager** и настройки Nginx для обслуживания бекенда по HTTPS.

### Обзор архитектуры

```
Клиент ──HTTPS:443──► Nginx (VM) ──HTTP:8080──► Spring Boot (Docker)
                         │
                    TLS-сертификат
                  из Yandex Cloud
                  Certificate Manager
```

### Предварительные требования

1. VM уже развёрнута в Yandex Cloud и доступна по публичному IP.
2. Установлен и настроен Nginx (см. раздел «Настройка Nginx» выше).
3. Есть доменное имя, чья A-запись указывает на публичный IP вашей VM.
4. Установлен CLI-клиент `yc` (Yandex Cloud CLI) и выполнена авторизация.
5. Установлен `jq` для парсинга JSON-ответов `yc`.

### Шаг 1 — Установка Yandex Cloud CLI

Если `yc` ещё не установлен:

```bash
# macOS
brew install yandex-cloud-cli

# Linux
curl -sSL https://storage.yandexcloud.net/yandexcloud-yc/install.sh | bash
exec bash

# Авторизация
yc init
```

После `yc init` выберите облако, каталог и создайте профиль по умолчанию.

### Шаг 2 — Создание сертификата в Certificate Manager

Yandex Cloud Certificate Manager поддерживает два типа сертификатов:

| Тип | Описание | Срок действия | Продление |
|-----|----------|---------------|-----------|
| **Let's Encrypt** | Бесплатный, автоматически проверяет домен через HTTP-01 | 90 дней | Автоматическое |
| **Custom** | Загрузка собственного сертификата | По сертификату | Вручную |

> **Рекомендация:** используйте **Let's Encrypt** — он бесплатный и автоматически продлевается.

#### 2.1. Создание Let's Encrypt сертификата

```bash
# Узнайте ID каталога
yc config list

# Создайте сертификат
yc certificate-manager certificate create \
  --name radio-backend-cert \
  --domains "your-domain.com" \
  --type letsencrypt \
  --description "TLS cert for Mordva Radio Backend"
```

> ⚠️ Замените `your-domain.com` на ваш реальный домен.

#### 2.2. Прохождение проверки домена (HTTP-01 challenge)

После создания сертификата Yandex Cloud сгенерирует challenge для подтверждения владения доменом. Получите данные проверки:

```bash
# Получите ID сертификата
CERT_ID=$(yc certificate-manager certificate list --format json | \
  jq -r '.[] | select(.name=="radio-backend-cert") | .id')

# Получите challenge
yc certificate-manager certificate get --id "$CERT_ID" --format json | \
  jq '.challenges'
```

В ответе вы увидите что-то вроде:

```json
{
  "challenges": [
    {
      "type": "HTTP",
      "created": "2025-01-15T10:00:00Z",
      "updated": "2025-01-15T10:00:00Z",
      "status": "PENDING",
      "message": "Create a file with the specified content at the specified path",
      "path": "/.well-known/acme-challenge/<TOKEN>",
      "content": "<KEY_AUTHORIZATION>"
    }
  ]
}
```

Создайте файл проверки на VM:

```bash
# Создайте директорию для ACME challenge
sudo mkdir -p /var/www/html/.well-known/acme-challenge

# Создайте файл с токеном (подставьте значения из ответа yc)
echo "<KEY_AUTHORIZATION>" | sudo tee /var/www/html/.well-known/acme-challenge/<TOKEN>

# Установите права
sudo chown -R www-data:www-data /var/www/html/.well-known
sudo chmod -R 755 /var/www/html/.well-known
```

Добавьте в Nginx блок обслуживания ACME challenge (в конфиг `radio-backend`):

```nginx
# Вставьте ВНЕ блока server { } или добавьте отдельный server block:
server {
    listen 80;
    server_name your-domain.com;

    # ACME challenge для Certificate Manager
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # Проксирование на Spring Boot
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo nginx -t && sudo systemctl reload nginx
```

#### 2.3. Проверка статуса сертификата

Через 5–15 минут Yandex Cloud проверит домен и выпустит сертификат:

```bash
yc certificate-manager certificate get --id "$CERT_ID" --format json | \
  jq '{status: .status, domains: .domains, not_after: .not_after}'
```

Статус должен измениться с `VALIDATING` на `ISSUED`.

### Шаг 3 — Скачивание сертификата на VM

Когда сертификат выпущен, скачайте цепочку и приватный ключ:

```bash
# Создайте директорию для сертификатов
sudo mkdir -p /etc/nginx/ssl/radio-backend
sudo chown $USER:$USER /etc/nginx/ssl/radio-backend

# Скачайте сертификат (цепочка)
yc certificate-manager certificate content \
  --id "$CERT_ID" \
  --chain > /etc/nginx/ssl/radio-backend/fullchain.pem

# Скачайте приватный ключ
yc certificate-manager certificate content \
  --id "$CERT_ID" \
  --private-key > /etc/nginx/ssl/radio-backend/privkey.pem

# Установите безопасные права
chmod 600 /etc/nginx/ssl/radio-backend/privkey.pem
chmod 644 /etc/nginx/ssl/radio-backend/fullchain.pem
```

### Шаг 4 — Настройка Nginx для HTTPS

Обновите конфиг `/etc/nginx/sites-available/radio-backend`:

```nginx
# HTTP → HTTPS редирект
server {
    listen 80;
    server_name your-domain.com;

    # ACME challenge (для продления сертификата)
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # Редирект всего остального на HTTPS
    location / {
        return 301 https://$host$request_uri;
    }
}

# HTTPS
server {
    listen 443 ssl;
    server_name your-domain.com;

    # Сертификат из Yandex Cloud Certificate Manager
    ssl_certificate     /etc/nginx/ssl/radio-backend/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/radio-backend/privkey.pem;

    # Рекомендуемые SSL-настройки (Mozilla Modern)
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # HSTS (опционально, требует уверенности в долгосрочной поддержке HTTPS)
    add_header Strict-Transport-Security "max-age=63072000" always;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Примените конфигурацию:

```bash
sudo nginx -t && sudo systemctl reload nginx
```

### Шаг 5 — Открытие порта 443 в файрволе

```bash
# UFW (Ubuntu)
sudo ufw allow 443/tcp

# iptables
sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
```

Также убедитесь, что в **security group** Yandex Cloud (в веб-консоли) разрешён входящий TCP-трафик на порт 443.

### Шаг 6 — Проверка

```bash
# Проверьте HTTPS-соединение
curl -v https://your-domain.com/api/city

# Проверьте редирект HTTP → HTTPS
curl -I http://your-domain.com/api/city
# Ожидается: 301 / 308 redirect на https://...

# Проверьте сертификат через openssl
echo | openssl s_client -connect your-domain.com:443 -servername your-domain.com 2>/dev/null | \
  openssl x509 -noout -dates -subject
```

### Автоматическое обновление сертификата

Сертификаты Let's Encrypt в Certificate Manager обновляются **автоматически** (Yandex Cloud сам проходит challenge и обновляет сертификат за ~30 дней до истечения). Однако **скачивание обновлённого сертификата на VM** нужно автоматизировать.

Создайте скрипт `/usr/local/bin/update-radio-cert.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

CERT_ID="<ВАШ_CERT_ID>"
SSL_DIR="/etc/nginx/ssl/radio-backend"

# Проверяем, что сертификат выпущен
STATUS=$(yc certificate-manager certificate get --id "$CERT_ID" --format json | jq -r '.status')
if [ "$STATUS" != "ISSUED" ]; then
    echo "Сертификат не в статусе ISSUED (текущий: $STATUS). Пропускаем."
    exit 0
fi

# Скачиваем обновлённый сертификат
yc certificate-manager certificate content \
  --id "$CERT_ID" \
  --chain > "$SSL_DIR/fullchain.pem"

yc certificate-manager certificate content \
  --id "$CERT_ID" \
  --private-key > "$SSL_DIR/privkey.pem"

chmod 600 "$SSL_DIR/privkey.pem"
chmod 644 "$SSL_DIR/fullchain.pem"

# Перезагружаем Nginx
nginx -t && systemctl reload nginx

echo "[$(date)] Сертификат обновлён и Nginx перезагружен."
```

```bash
sudo chmod +x /usr/local/bin/update-radio-cert.sh
```

Добавьте в cron (запуск раз в неделю):

```bash
# Откройте crontab root'а
sudo crontab -e

# Добавьте строку:
0 4 * * 1 /usr/local/bin/update-radio-cert.sh >> /var/log/update-radio-cert.log 2>&1
```

> Скрипт скачивает сертификат каждую неделю в 04:00 понедельника. Если сертификат не изменился, Nginx просто перезагрузится с тем же файлом — это безопасно.

### Альтернатива: Certbot (без Certificate Manager)

Если вы предпочитаете **не использовать** Yandex Cloud Certificate Manager, можно установить Certbot напрямую на VM:

```bash
# Установка Certbot
sudo apt install certbot python3-certbot-nginx -y

# Получение сертификата (Certbot сам изменит конфиг Nginx)
sudo certbot --nginx -d your-domain.com

# Автоматическое продление уже настроено через systemd timer:
sudo systemctl status certbot.timer
```

> **Плюс Certbot:** не нужен `yc` CLI, всё работает локально на VM.
> **Плюс Certificate Manager:** централизованное управление сертификатами в облаке, интеграция с другими сервисами Yandex Cloud (ALB, CDN, API Gateway).

### Устранение неполадок

| Проблема | Решение |
|----------|---------|
| Сертификат застрял в `VALIDATING` | Проверьте что ACME-challenge файл доступен по `http://your-domain.com/.well-known/acme-challenge/<TOKEN>` |
| `curl: (60) SSL certificate problem` | Убедитесь что скачан `fullchain.pem`, а не только `cert.pem` — нужен полный chain |
| Nginx: `SSL: error:0B080074:x509 certificate routines:X509_check_private_key:key values mismatch` | Приватный ключ не соответствует сертификату — перескачайте оба файла заново |
| `ERR_SSL_PROTOCOL_ERROR` в браузере | Проверьте что порт 443 открыт в файрволе и security group Yandex Cloud |
| Сертификат истёк | Проверьте cron-задачу обновления: `sudo crontab -l` и лог `/var/log/update-radio-cert.log` |

---

## Структура Docker-файлов

```
backend_radio/
├── Dockerfile              # Многоэтапная сборка (Gradle → JRE)
├── docker-compose.yml      # Только Spring Boot (БД уже на VM)
├── .dockerignore            # Исключения для Docker-контекста
├── .env.example            # Шаблон переменных окружения
└── DEPLOY.md               # Эта инструкция
```

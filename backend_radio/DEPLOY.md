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

## Структура Docker-файлов

```
backend_radio/
├── Dockerfile              # Многоэтапная сборка (Gradle → JRE)
├── docker-compose.yml      # Только Spring Boot (БД уже на VM)
├── .dockerignore            # Исключения для Docker-контекста
├── .env.example            # Шаблон переменных окружения
└── DEPLOY.md               # Эта инструкция
```

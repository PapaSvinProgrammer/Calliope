# 🚀 Деплой Mordva Radio Backend на виртуальную машину

## Содержание
1. [Требования к VM](#требования-к-vm)
2. [Быстрый старт](#быстрый-старт)
3. [Пошаговая инструкция](#пошаговая-инструкция)
4. [Проверка работы](#проверка-работы)
5. [Управление](#управление)
6. [Настройка Nginx (опционально)](#настройка-nginx-опционально)

---

## Требования к VM

| Ресурс   | Минимум      |
|----------|-------------|
| OS       | Ubuntu 20.04+ / Debian 11+ |
| CPU      | 1 ядро      |
| RAM      | 2 ГБ        |
| Disk     | 10 ГБ       |
| Docker   | 24+         |
| Compose  | v2+         |

---

## Быстрый старт

```bash
# 1. Клонируйте репозиторий на VM
git clone <URL_РЕПОЗИТОРИЯ> radio-backend
cd radio-backend

# 2. Создайте .env из шаблона и задайте пароль
cp .env.example .env
nano .env   # укажите реальный пароль для PostgreSQL

# 3. Запустите всё одной командой
docker compose up -d --build

# 4. Готово! Бекенд доступен по адресу:
# http://<IP_МАШИНЫ>:8080/api/city
```

---

## Пошаговая инструкция

### Шаг 1 — Установка Docker и Docker Compose

Если на VM ещё нет Docker:

```bash
# Обновляем пакеты
sudo apt update && sudo apt upgrade -y

# Устанавливаем Docker
curl -fsSL https://get.docker.com | sudo sh

# Добавляем текущего пользователя в группу docker (чтобы без sudo)
sudo usermod -aG docker $USER
newgrp docker

# Проверяем установку
docker --version
docker compose version
```

### Шаг 2 — Копирование проекта на VM

**Вариант A — через Git (рекомендуется):**
```bash
git clone <URL_РЕПОЗИТОРИЯ> radio-backend
cd radio-backend
```

**Вариант B — через scp (если нет Git-репозитория):**
```bash
# На локальной машине:
scp -r ./backend_radio user@<IP_VM>:~/radio-backend

# На VM:
cd ~/radio-backend
```

### Шаг 3 — Настройка переменных окружения

```bash
# Копируем шаблон
cp .env.example .env

# Редактируем — обязательно задайте надёжный пароль!
nano .env
```

Содержимое `.env`:
```env
POSTGRES_DB=radio
POSTGRES_USER=radio
POSTGRES_PASSWORD=ваш_надёжный_пароль_здесь
```

### Шаг 4 — Запуск

```bash
# Собираем образ и запускаем контейнеры
docker compose up -d --build
```

При первом запуске:
- Gradle скачает зависимости и соберёт JAR (~2-5 мин)
- PostgreSQL создаст базу данных
- Приложение запустится на порту 8080

### Шаг 5 — Проверка логов

```bash
# Логи приложения
docker compose logs -f app

# Логи базы данных
docker compose logs -f db
```

---

## Проверка работы

После успешного запуска бекенд будет доступен по IP виртуальной машины:

```bash
# Проверка health (если есть actuator) или просто API
curl http://<IP_VM>:8080/api/city

# Список радиостанций
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
# Остановить все контейнеры
docker compose down

# Остановить и удалить данные БД (⚠️ все данные пропадут!)
docker compose down -v

# Пересобрать после изменений в коде
docker compose up -d --build

# Посмотреть статус контейнеров
docker compose ps

# Рестарт только приложения (без пересборки)
docker compose restart app
```

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

## Открытие портов

Если бекенд недоступен извне, убедитесь что порты открыты в файрволе:

```bash
# UFW (Ubuntu)
sudo ufw allow 8080/tcp
sudo ufw allow 80/tcp   # если настроен Nginx

# Или iptables
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
```

Также проверьте настройки **security group / firewall** в панели управления облачным провайдером — порт 8080 должен быть открыт для входящих TCP-соединений.

---

## Структура Docker-файлов

```
backend_radio/
├── Dockerfile              # Многоэтапная сборка (Gradle → JRE)
├── docker-compose.yml      # PostgreSQL + Spring Boot
├── .dockerignore            # Исключения для Docker-контекста
├── .env.example            # Шаблон переменных окружения
└── DEPLOY.md               # Эта инструкция
```

# lab3

Клиент-серверное приложение на Spring Boot с обменом по HTTPS и взаимной TLS-аутентификацией клиента и сервера.

## Что реализовано

- HTTPS REST-сервер на Spring Boot.
- Клиентский CLI-режим, который отправляет `POST`-запрос по HTTPS с клиентским сертификатом.
- Настроен `two-way TLS`: сервер требует сертификат клиента, клиент проверяет сертификат сервера.
- Добавлены скрипты генерации `CA`, `keystore` и `truststore`.
- Добавлены интеграционные тесты для успешного и неуспешного mutual TLS handshake.

## Структура TLS-материалов

Файлы лежат в `app/src/main/resources/tls`:

- `ca.crt` - сертификат учебного центра сертификации.
- `server-keystore.p12` - серверный ключ и сертификат.
- `server-truststore.p12` - доверие сервера к CA клиента.
- `client-keystore.p12` - клиентский ключ и сертификат.
- `client-truststore.p12` - доверие клиента к CA сервера.

Пароль для учебных `PKCS12`-хранилищ: `changeit`.

## Генерация сертификатов

Команды выполняются из директории `lab3`. Для генерации нужен `openssl`.

Linux/macOS/Git Bash:

```bash
chmod +x app/scripts/generate-certs.sh
./app/scripts/generate-certs.sh
```

PowerShell:

```powershell
./app/scripts/generate-certs.ps1
```

Скрипты создают материалы в `app/src/main/resources/tls`.

## Запуск через Gradle

Сервер:

```bash
./gradlew :app:bootRun --args='server'
```

Клиент:

```bash
./gradlew :app:bootRun --args='client "Hello mutual TLS"'
```

Клиент с явным URL:

```bash
./gradlew :app:bootRun --args='client "Hello mutual TLS" https://localhost:8443/api/messages'
```

Короткий демо-режим клиента:

```bash
./gradlew :app:bootRun --args='demo'
```

## Запуск JAR

Сборка:

```bash
./gradlew :app:bootJar
```

Запуск сервера:

```bash
java -jar app/build/libs/app.jar server
```

Запуск клиента:

```bash
java -jar app/build/libs/app.jar client "Hello mutual TLS"
```

## Тесты

```bash
./gradlew :app:test
./gradlew :app:test --tests lab3.MutualTlsIntegrationTest
```

## Docker

Сборка выполняется из директории `lab3`.

```bash
docker build -t lab3-mtls .
```

Запуск сервера:

```bash
docker run --rm -p 8443:8443 --name lab3-server lab3-mtls
```

Запуск клиента в том же образе против локального сервера:

```bash
docker run --rm --network host lab3-mtls client "Hello mutual TLS" https://localhost:8443/api/messages
```

Пример двух контейнеров в одной сети Docker:

```bash
docker network create lab3-net
docker run --rm -d --network lab3-net --name lab3-server lab3-mtls
docker run --rm --network lab3-net lab3-mtls client "Hello mutual TLS" https://lab3-server:8443/api/messages
```

## В чем сложность контейнеризации

- Mutual TLS требует отдельные ключи и truststore для сервера и клиента, а не один общий сертификат.
- Для контейнерного клиента серверный сертификат должен содержать корректный `SAN`, например `localhost` и `lab3-server`, иначе проверка hostname завершится ошибкой.
- В учебном образе серверные и клиентские TLS-материалы лежат внутри одного JAR, что удобно для демонстрации, но в реальном проекте так делать не стоит.
- В production секреты обычно передают через `Docker secrets`, `Kubernetes secrets` или смонтированные volume, а не коммитят в репозиторий и не встраивают в образ.

## Примечание по безопасности

Текущие сертификаты и пароли учебные. Они сделаны для воспроизводимой демонстрации `two-way TLS`, а не для промышленной эксплуатации.

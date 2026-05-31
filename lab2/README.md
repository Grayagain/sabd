# lab2

Консольная Java-утилита для CMS-шифрования, расшифровки, подписи и проверки подписи на Bouncy Castle.

## Что изменилось

- Добавлены CLI-команды `encrypt`, `decrypt`, `sign`, `verify`, `roundtrip`, `demo`.
- Бинарные CMS-данные теперь выводятся в Base64.
- В `verify` выводится статус проверки и встроенное подписанное содержимое.
- Добавлен Docker-образ для запуска без локальной настройки Java.

## Важно

- Для обычного JVM-запуска рабочая директория должна быть `lab2/app`, потому что приложение читает `public.cer` и `private.p12` по относительным путям.
- Проект собирается с Java 8 toolchain через Gradle.

## Запуск через Gradle

Команды выполняются из директории `lab2`.

```bash
./gradlew run --args='demo'
./gradlew run --args='demo "Hello CMS"'
./gradlew run --args='encrypt "secret"'
./gradlew run --args='sign "secret"'
./gradlew run --args='roundtrip "secret"'
```

Если нужен прямой запуск `lab2.App` из IDE или JVM, рабочую директорию выставляй в `lab2/app`.

## CLI-команды

```text
demo [message]
encrypt <message>
decrypt <base64-cms>
sign <message>
verify <base64-cms>
roundtrip <message>
```

## Тесты

Из директории `lab2`:

```bash
./gradlew test
./gradlew test --tests lab2.AppTest
```

## Docker

Сборка выполняется из директории `lab2`.

```bash
docker build -t lab2-cms .
```

Примеры запуска:

```bash
docker run --rm lab2-cms
docker run --rm lab2-cms demo "Hello CMS"
docker run --rm lab2-cms encrypt "secret"
docker run --rm lab2-cms roundtrip "Hello CMS"
```

Проверено, что контейнер:

- успешно собирается;
- читает `public.cer` и `private.p12` внутри образа;
- корректно выполняет `demo`, `encrypt` и `roundtrip`.

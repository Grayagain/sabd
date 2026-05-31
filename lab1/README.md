# lab1

Консольная Java-утилита для обфускации и деобфускации текстовых данных в XML.

## Сборка Docker-образа

```bash
docker build -t lab1-xml-obfuscator .
```

Команду нужно выполнять из директории `lab1`.

## Запуск через Docker

Чтобы контейнер видел входные и выходные XML-файлы, примонтируй папку `app/data`:

```bash
docker run --rm -v "${PWD}/app/data:/data" lab1-xml-obfuscator obfuscate /data/input.xml /data/output.xml
```

Обратное восстановление:

```bash
docker run --rm -v "${PWD}/app/data:/data" lab1-xml-obfuscator deobfuscate /data/output.xml /data/restored.xml
```

Обработка и текста, и XML-атрибутов:

```bash
docker run --rm -v "${PWD}/app/data:/data" lab1-xml-obfuscator obfuscate /data/input.xml /data/output.xml --attributes
```

## Запуск в IntelliJ IDEA

Если Docker не нужен, можно запускать главный класс `lab1.App` с аргументами:

```text
obfuscate app/data/input.xml app/data/output.xml
deobfuscate app/data/output.xml app/data/restored.xml
obfuscate app/data/input.xml app/data/output.xml --attributes
```

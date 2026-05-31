## Project Shape

- This repo is a single Gradle project with one subproject: `:app` from `settings.gradle`. It is a plain Java CLI app, not Android.
- Main entrypoint: `app/src/main/java/lab1/App.java` with `mainClass = 'lab1.App'` in `app/build.gradle`.
- Core logic lives in `lab1.XmlProcessor` and `lab1.Obfuscator`.

## Commands

- Use the Gradle wrapper from repo root: `./gradlew test`, `./gradlew run --args='obfuscate app/data/input.xml app/data/output.xml'`.
- For module-scoped work, use `./gradlew :app:test` and `./gradlew :app:run --args='deobfuscate app/data/output.xml app/data/restored.xml'`.
- There is no separate lint/typecheck setup in this repo; `test` is the main verification task.

## Environment Gotchas

- `gradle-wrapper.properties` pins Gradle `9.2.0`.
- `settings.gradle` applies `org.gradle.toolchains.foojay-resolver-convention`, and `app/build.gradle` requests Java toolchain `8`.
- You still need a local JVM available to start `./gradlew` at all. In this workspace, wrapper commands fail if `JAVA_HOME` is unset and `java` is missing from `PATH`.

## Behavior That Is Easy To Miss

- CLI usage is exactly `obfuscate|deobfuscate input.xml output.xml`; `App.main` expects 3 args.
- Sample XML fixtures live in `app/data/`: `input.xml`, `output.xml`, and `restored.xml`.
- `XmlProcessor` only rewrites text nodes. XML attributes such as `employee id="111"` are preserved unchanged.
- `XmlProcessor` trims text before deciding whether to transform it, then writes with `Transformer` indentation enabled, so output formatting may differ from input even when content round-trips.

## Tests

- Current automated coverage is minimal: `app/src/test/java/lab1/ObfuscatorTest.java` only checks obfuscation round-trip.
- If you change `App` or `XmlProcessor`, add or run focused tests for CLI argument handling and XML traversal; the existing test suite will not catch those regressions.

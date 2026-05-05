# Repository Guidelines

## Project Structure & Module Organization
This repository is a single-module Android/Xposed project. Root Gradle files (`build.gradle`, `settings.gradle`, `gradle.properties`) define shared configuration. The only module is `app/`.

Source code lives in `app/src/main/java/com/example/miui/folderfix/`, with the Xposed entry point in `MainHook.java`. Android metadata is in `app/src/main/AndroidManifest.xml`. Xposed registration is handled by `app/src/main/assets/xposed_init`. Build outputs are generated under `app/build/`.

## Build, Test, and Development Commands
Use JDK 17 and a local Gradle installation; this repo does not include `gradlew`.

- `gradle assembleDebug` builds the debug APK used by CI.
- `gradle assembleRelease` builds a release APK with ProGuard rules from `app/proguard-rules.pro`.
- `gradle clean` removes module build artifacts.

GitHub Actions runs the same debug build in `.github/workflows/build.yml`, so local changes should pass `gradle assembleDebug` before opening a PR.

## Coding Style & Naming Conventions
Java 17 is the project baseline. Follow the existing style in `MainHook.java`: 4-space indentation, braces on the same line, and descriptive `camelCase` field and method names. Keep package names lowercase and class names `PascalCase`.

This module hooks `com.miui.home` and uses reflection/Xposed APIs. Prefer small, defensive changes, and document non-obvious hook behavior with short comments only when needed.

## Testing Guidelines
There is no dedicated test suite yet. Treat a successful `gradle assembleDebug` as the minimum validation step.

For behavior changes, verify on a compatible LSPosed/Xposed setup and confirm the hook still loads for `com.miui.home`. In PRs, note the device, Android version, and MIUI/Home version used for manual testing.

## Commit & Pull Request Guidelines
Recent history favors short, imperative subjects and conventional prefixes such as `fix:` and `chore:`. Continue that pattern, for example: `fix: guard hidden API lookup`.

PRs should include a concise summary, the reason for the change, manual validation steps, and any affected MIUI/Home versions. Add screenshots only if you change user-visible app metadata or installation behavior.

## Security & Configuration Notes
Do not broaden the hook scope without clear justification. Keep the target package, reflected methods, and resource identifiers narrowly scoped to reduce breakage across MIUI updates.

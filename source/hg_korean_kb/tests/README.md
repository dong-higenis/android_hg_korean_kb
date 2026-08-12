# Host-side unit tests (not part of the APK)

These Java sources are **excluded** from the Soong `android_app` `srcs` glob.

| File | Purpose |
|------|---------|
| `src/com/higenis/keyboard/input/HangulComposerTest.java` | HangulComposer JVM tests |
| `src/com/higenis/keyboard/InputTypeHelperTest.java` | InputTypeHelper bit-mask tests |

They are kept for local JVM bring-up. AOSP `java_test_host` / instrumented modules are optional later work — not required for STEP 12 product integration.

Production APK / Soong sources live only under `../app/src/main/`.

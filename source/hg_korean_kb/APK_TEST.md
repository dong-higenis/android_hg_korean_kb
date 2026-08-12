# HG Keyboard — Gradle APK validation (device test)

Temporary APK packaging for Android 10 device validation.
Production IME logic stays under `app/src/main/` and is shared with AOSP Soong (`Android.bp`).

| Item | Value |
|------|-------|
| applicationId | `com.higenis.keyboard` |
| IME service | `.HGKeyboardService` |
| IME ID | `com.higenis.keyboard/.HGKeyboardService` |
| min / target / compile SDK | 29 |
| AndroidX / Compose / libs | none |

---

## Project structure

```
hg_korean_kb/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── Android.bp                         # AOSP only (not used by Gradle)
├── aosp/product/hgkeyboard.mk         # AOSP product helper
├── app/                               # HG Keyboard IME APK
│   └── src/main/...
├── testapp/                           # inputType host app (validation only)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/.../MainActivity.java
│       └── res/layout/activity_main.xml
└── tests/                             # host JVM tests (not in APK)
```

---

## Build

Requirements:

- JDK 11+ (JDK 17 OK with this Gradle/AGP set)
- Android SDK with `platforms;android-29`
- `local.properties` with `sdk.dir=...` (gitignored; create locally)

```bash
# Windows — both IME + test host
gradlew.bat assembleDebug

# Or individually
gradlew.bat :app:assembleDebug
gradlew.bat :testapp:assembleDebug
```

---

## APK output

```
app/build/outputs/apk/debug/app-debug.apk
testapp/build/outputs/apk/debug/testapp-debug.apk
```

| Module | applicationId | Role |
|--------|---------------|------|
| `:app` | `com.higenis.keyboard` | IME |
| `:testapp` | `com.higenis.keyboard.test` | EditText matrix host |

---

## Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r testapp/build/outputs/apk/debug/testapp-debug.apk
```

---

## Enable IME in Settings

1. Settings → System → Languages & input → Virtual keyboard → Manage keyboards  
   (wording may vary slightly by OEM)
2. Enable **HG Keyboard**
3. Launch **HG Keyboard Test** → tap a field → select **HG Keyboard**

---

## adb IME commands

```bash
adb shell ime list -a
adb shell ime list -s
adb shell ime enable com.higenis.keyboard/.HGKeyboardService
adb shell ime set com.higenis.keyboard/.HGKeyboardService
adb shell pm path com.higenis.keyboard
adb shell am start -n com.higenis.keyboard.test/.MainActivity
```

---

## Recommended test order (in HG Keyboard Test)

1. Enable / set HG Keyboard (Settings or `ime enable` / `ime set`)
2. **text** — 한글 조합, 복합 모음/종성, 한/영, Shift/Caps, ?123, Backspace 분해
3. **textMultiLine** — Enter = newline; composition then move focus
4. Jump **text → number → text** — composition reset on field change
5. **number** / **numberDecimal** / **numberSigned** / **signed+decimal**
6. **phone** — 0–9, `*`, `#`
7. **email** / **textUri** — TEXT layout (한/영, symbols)
8. **textPassword** / **numberPassword** — layout + no typed chars in logcat

---

## Re-integrating into AOSP later

Reuse these unchanged production assets:

| Gradle path | AOSP use |
|-------------|----------|
| `app/src/main/java/...` | Soong `srcs` (already pointed by `Android.bp`) |
| `app/src/main/res/...` | Soong `resource_dirs` |
| `app/src/main/AndroidManifest.xml` | Soong `manifest` |
| `Android.bp` | module definition |
| `aosp/product/hgkeyboard.mk` | `PRODUCT_PACKAGES` helper |

Do **not** copy Gradle files (`build.gradle`, `settings.gradle`, `gradle/`, `.gradle`, `app/build`) into the product image story — only the Java/res/manifest/`Android.bp` matter for Soong.

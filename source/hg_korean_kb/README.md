# HG Keyboard — Android 10 AOSP IME (STEP 12)

Korean/English QWERTY system Input Method for **Android 10 / AOSP 10 (API 29)**.

| Item | Value |
|------|-------|
| Tree path | `packages/inputmethods/HGKeyboard/` |
| Package | `com.higenis.keyboard` |
| Soong module | `HGKeyboard` |
| IME service | `com.higenis.keyboard.HGKeyboardService` |
| IME ID | `com.higenis.keyboard/.HGKeyboardService` |

Sources are shared by:

1. **Gradle APK** (device validation) — see [APK_TEST.md](APK_TEST.md)
2. **AOSP Soong** (`Android.bp` → `packages/inputmethods/HGKeyboard/`)

No AndroidX, Material, Kotlin, or external JARs.

**STEP 12 (AOSP) is paused** while APK validation runs on a physical Android 10 device.  
**Not yet:** product default IME / `enabled_input_methods` (→ STEP 13).

---

## 1. Source layout (shared)

```
hg_korean_kb/   (= packages/inputmethods/HGKeyboard/ in AOSP)
├── Android.bp
├── aosp/product/hgkeyboard.mk
├── settings.gradle / build.gradle / app/build.gradle   # APK only
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/higenis/keyboard/   # production Java
│   └── res/                         # layout, values, xml/method.xml
└── tests/src/...                    # host tests — not in APK / Soong srcs
```

Copy the whole module into AOSP when resuming integration:

```bash
mkdir -p packages/inputmethods
cp -a /path/to/hg_korean_kb packages/inputmethods/HGKeyboard
```

---

## 2. Android.bp (final)

```bp
android_app {
    name: "HGKeyboard",
    srcs: ["app/src/main/java/com/higenis/keyboard/**/*.java"],
    resource_dirs: ["app/src/main/res"],
    manifest: "app/src/main/AndroidManifest.xml",
    sdk_version: "current",
}
```

| Property | Decision |
|----------|----------|
| `sdk_version: "current"` | Compile against the platform public SDK of this tree (API 29 on AOSP 10). |
| `platform_apis` | **Omitted** — public IME APIs only. |
| `certificate: "platform"` | **Omitted** — normal system app signing is enough. |
| `privileged: true` | **Omitted** — not a privileged app. |
| `static_libs` / AndroidX | **None**. |

`srcs` is limited to `app/src/main/java/...` so `tests/` never enters the APK.

### Public API check (API 29)

Used framework types are public on Android 10:

- `InputMethodService`, `EditorInfo`, `InputType`
- `InputConnection` including `deleteSurroundingTextInCodePoints` (API 24+)
- `onUpdateSelection` override on `InputMethodService`

No hidden/internal APIs are required.

---

## 3. Manifest

- Service: `.HGKeyboardService`
- Permission: `android.permission.BIND_INPUT_METHOD`
- Intent: `android.view.InputMethod`
- Meta-data: `android.view.im` → `@xml/method`
- No `INTERNET`, no Activities, no `directBootAware`
- `uses-sdk` min/target 29 (harmless alongside Soong `sdk_version`)

---

## 4. PRODUCT_PACKAGES — where to add

### Finding the right makefile

1. `source build/envsetup.sh && lunch`
2. Note the chosen product (e.g. `aosp_arm64-userdebug` or vendor `xxx-userdebug`).
3. Open `device/**/AndroidProducts.mk` (or `vendor/**`) and find the `.mk` listed for that lunch combo.
4. Follow `$(call inherit-product, …)` until you reach the product/device makefile that owns **this device’s app set** (often `device/<vendor>/<product>/device.mk` or `<product>.mk`).
5. Add **once**:

```makefile
PRODUCT_PACKAGES += \
    HGKeyboard
```

Or inherit the helper fragment shipped with this module:

```makefile
$(call inherit-product, packages/inputmethods/HGKeyboard/aosp/product/hgkeyboard.mk)
```

### Inheritance sketch

```
AndroidProducts.mk
  → PRODUCT_MAKEFILES += …/<product>.mk
       → inherit-product …/device.mk
            → inherit-product …/common.mk
                 → PRODUCT_PACKAGES += …
```

Put `HGKeyboard` in **one** file on that chain. Duplicating across parent + child is unnecessary noise (usually harmless, but avoid).

---

## 5. Build environment

From AOSP root (Linux build host typical for Android 10):

```bash
source build/envsetup.sh
lunch <product>-userdebug
```

### Single-module build (recommended first)

```bash
m HGKeyboard
```

`m <module>` uses the Soong module graph and is preferred on Android 10.

`mmm packages/inputmethods/HGKeyboard` may still work on some trees but is the older Make path; prefer `m HGKeyboard`.

### Full product image

```bash
m -j$(nproc)
# or
make -j$(nproc)
```

Then flash with the product’s usual flow (`fastboot flashall`, vendor flash script, etc.).

---

## 6. Build failure checklist

| Symptom | Where to look |
|---------|----------------|
| Java compile error | `out/soong/.intermediates/.../HGKeyboard/...` javac log; package/path mismatch |
| Resource / aapt error | `res/` duplicate names, missing `@string` / `@xml` |
| Manifest error | service name, `BIND_INPUT_METHOD`, meta-data |
| Soong property error | `Android.bp` unknown keys for this branch |
| Unsupported API | accidental post-29 / hidden API |
| Test classes in APK | ensure tests are under `tests/`, not `src/` |

---

## 7. Finding the APK

Paths vary by product and Soong version. Search after a successful module build:

```bash
find out/target/product -name 'HGKeyboard.apk' 2>/dev/null
find out/soong/.intermediates -name 'HGKeyboard.apk' 2>/dev/null
```

Do not hard-code a single intermediates path across BSPs.

---

## 8. System image location

With `PRODUCT_PACKAGES += HGKeyboard` and a normal `android_app` (not privileged), expect something like:

`/system/app/HGKeyboard/HGKeyboard.apk`

Product/system_ext/partition layout can differ. After flash, trust:

```bash
adb shell pm path com.higenis.keyboard
```

Example:

```
package:/system/app/HGKeyboard/HGKeyboard.apk
```

---

## 9. Package / IME verification (adb)

### Package

```bash
adb shell pm path com.higenis.keyboard
adb shell pm list packages com.higenis.keyboard
```

Windows host without `grep`: prefer the `pm list packages <filter>` form above (filter is applied by `pm`).

If the device shell has `grep`:

```bash
adb shell "pm list packages | grep higenis"
```

### IME registration (Android 10 `ime` tool)

```bash
adb shell ime list -a          # all IMEs (enabled + disabled)
adb shell ime list -s          # brief IDs of enabled IMEs
adb shell ime list             # enabled, verbose
```

Expect ID:

```
com.higenis.keyboard/.HGKeyboardService
```

### Dev-only enable / select (not product default)

```bash
adb shell ime enable com.higenis.keyboard/.HGKeyboardService
adb shell ime set com.higenis.keyboard/.HGKeyboardService
```

These are supported on Android 10 (`ime enable` / `ime set` / `ime list [-a][-s]`).

### Debug queries (STEP 12 = inspection only)

```bash
adb shell settings get secure default_input_method
adb shell settings get secure enabled_input_methods
```

Product-level default wiring is **STEP 13** — do not bake SettingsProvider overlays in this STEP.

---

## 10. Manual input smoke test (after enable/set)

Open an app with `EditText` and verify:

**TEXT:** Hangul, English, Shift/Caps, 한/영, ?123, compound vowels/finals  
**NUMBER:** integer / decimal / signed / signed-decimal  
**PHONE:** 0–9, `*`, `#`  
**Backspace:** Hangul decompose, plain text, emoji/code-point delete  

---

## 11. logcat

```bash
adb logcat -s HGKeyboard
```

`DebugLog.DEBUG` defaults to **`false`**. For eng bring-up only, temporarily set it to `true` in `DebugLog.java` and rebuild the module.

**Never** log typed characters in release. Call sites log lengths / enums / actions only.

---

## 12. Security posture

| Check | Status |
|-------|--------|
| INTERNET permission | None |
| Persist typed text | No (prefs store language only) |
| Clipboard harvest | No |
| Telemetry | No |
| External storage writes | No |
| Typed text in release logs | No (`DEBUG=false`; no char payloads) |

---

## 13. Incremental rebuild (avoid full clean)

If `Android.bp` or sources change:

```bash
m HGKeyboard
```

If Soong seems stale after bp edits:

```bash
# regenerate / rebuild module — usually enough
m HGKeyboard
```

Avoid `make clean` / wiping all of `out/` unless the tree is badly corrupted — full AOSP clean rebuilds are expensive. Last resort only.

---

## 14. Integration checklist

- [ ] A. `packages/inputmethods/HGKeyboard` present  
- [ ] B. `Android.bp` valid (no platform_apis / platform cert / privileged)  
- [ ] C. `m HGKeyboard` succeeds  
- [ ] D. `PRODUCT_PACKAGES += HGKeyboard` once in product chain  
- [ ] E. Full product build succeeds  
- [ ] F. Flash image  
- [ ] G. `adb shell pm path com.higenis.keyboard`  
- [ ] H. `adb shell ime list -a` shows HGKeyboard service  
- [ ] I. `adb shell ime enable com.higenis.keyboard/.HGKeyboardService`  
- [ ] J. `adb shell ime set com.higenis.keyboard/.HGKeyboardService`  
- [ ] K. Text / Number / Phone manual input test  

---

## 15. Troubleshooting

| Issue | Action |
|-------|--------|
| Module not found by `m` | Confirm path under `packages/inputmethods/HGKeyboard` and `name: "HGKeyboard"` |
| APK missing from image | Confirm `PRODUCT_PACKAGES` on the lunch product’s inherit chain |
| `pm path` empty | Rebuild product, reflash; check for install failures in logcat |
| Missing from `ime list -a` | Manifest service / `method.xml` / `BIND_INPUT_METHOD` |
| In `ime list -a` but not `-s` | Not enabled yet → `ime enable` |
| `ime set` fails | Enable first; confirm exact IME ID |
| Compile picks up tests | Move `*Test.java` under `tests/`; keep `srcs` under `src/` only |

---

## 16. Out of scope (STEP 13+)

- `default_input_method` / `enabled_input_methods` product defaults  
- SettingsProvider / SetupWizard / framework IME forcing  
- `privileged` / `certificate: "platform"`

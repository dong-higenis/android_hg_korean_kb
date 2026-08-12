# HG Keyboard를 기본 입력기로 설정 (Allwinner A133 / Android 10)

APK 설치 후 설정에서 켜는 것과 달리, **출하 이미지에서 처음부터 HG Keyboard가 기본 IME**가 되게 하는 방법입니다.

| 항목 | 값 |
|------|-----|
| IME ID | `com.higenis.keyboard/.HGKeyboardService` |
| Secure 키 | `default_input_method`, `enabled_input_methods` |
| 선행 조건 | `PRODUCT_PACKAGES += HGKeyboard` (이미지에 APK 포함) |

---

## 1. 원리

Android는 `Settings.Secure`에 IME 상태를 저장합니다.

| Secure 키 | 의미 |
|-----------|------|
| `default_input_method` | 현재 선택된 기본 IME |
| `enabled_input_methods` | 사용 가능한 IME 목록 (`:` 구분) |

공장 초기화 / 최초 부팅 시 `SettingsProvider`가
`res/values/defaults.xml` 값을 DB에 넣습니다.

→ **defaults를 overlay**하면 출하 기본 키보드가 바뀝니다.

이미 한 번 부팅된 기기는 DB가 남아 있어 overlay만으로는 안 바뀔 수 있습니다.
그때는 **데이터 초기화(factory reset)** 또는 아래 검증용 `settings put`을 사용합니다.

---

## 2. BSP에서 리소스 이름 확인 (필수)

Allwinner / 벤더 트리마다 string 이름이 다를 수 있습니다.

Android 소스 루트에서:

```bash
rg -n "input_method|INPUT_METHOD" frameworks/base/packages/SettingsProvider --glob '*.{xml,java}'
```

확인할 것:

1. `res/values/defaults.xml` (또는 벤더 overlay)에 어떤 `string name`이 있는지  
2. `DatabaseHelper.java` / `SettingsProvider`가 그 이름을 `loadStringSetting` / `loadSetting` 하는지  

### 자주 보는 조합 (Android 10 OEM)

**A. Allwinner 등에서 흔함**

```xml
<string name="def_default_input_method">...</string>
<string name="def_enabled_input_methods">...</string>
```

**B. 다른 트리**

```xml
<string name="def_input_method">...</string>
<string name="def_enabled_input_methods">...</string>
```

이 모듈에 넣어 둔 예시 overlay는 **둘 다** 적어 두었습니다.
사용 전 **자기 BSP에 없는 name은 제거**하세요. (없는 name만 있으면 merge는 되지만 로드가 안 됨)

이미 `device/softwinner/.../overlay/.../SettingsProvider/.../defaults.xml`이 있으면
**그 파일을 수정**하는 편이 더 안전합니다 (중복 overlay 충돌 방지).

---

## 3. 적용 방법 (추천: product inherit)

### 3-1. 소스 배치

```bash
cp -a hg_korean_kb packages/inputmethods/HGKeyboard
```

### 3-2. product makefile

`device/softwinner/ceres-c3/device.mk` (실제 보드 경로)에 **한 번만**:

```makefile
$(call inherit-product, packages/inputmethods/HGKeyboard/aosp/product/hgkeyboard_default_ime.mk)
```

이 파일은 다음을 합니다.

- `PRODUCT_PACKAGES += HGKeyboard`
- `PRODUCT_PACKAGE_OVERLAYS += packages/inputmethods/HGKeyboard/aosp/overlay`

이전에 `hgkeyboard.mk`만 inherit 했다면 **default_ime mk로 교체**하세요 (중복 `PRODUCT_PACKAGES` 방지).

### 3-3. overlay 내용 맞추기

```text
packages/inputmethods/HGKeyboard/aosp/overlay/
  frameworks/base/packages/SettingsProvider/res/values/defaults.xml
```

자기 BSP에 맞게 string name을 정리한 뒤, 값은 다음처럼 유지합니다.

```xml
<!-- 기본 선택 -->
com.higenis.keyboard/.HGKeyboardService

<!-- 활성화 목록 예: HG + LatinIME 폴백 -->
com.higenis.keyboard/.HGKeyboardService:com.android.inputmethod.latin/.LatinIME
```

LatinIME을 아예 빼려면 enabled 목록에서 제거하면 됩니다.
(문제 발생 시 복구가 어려우니 개발 중에는 남겨 두는 것을 권장)

---

## 4. 기존 softwinner overlay가 있을 때

```bash
find device/softwinner -path '*SettingsProvider*defaults.xml'
```

파일이 있으면 그 안에 아래를 **추가/수정**하는 것이 더 확실합니다.

```xml
<string name="def_default_input_method" translatable="false">com.higenis.keyboard/.HGKeyboardService</string>
<string name="def_enabled_input_methods" translatable="false">com.higenis.keyboard/.HGKeyboardService:com.android.inputmethod.latin/.LatinIME</string>
```

(이름은 2절에서 확인한 것으로 교체)

그리고 `PRODUCT_PACKAGES += HGKeyboard`만 device.mk에 유지하면 됩니다.
이 경우 `hgkeyboard_default_ime.mk`의 overlay 줄은 쓰지 않아도 됩니다.

---

## 5. DatabaseHelper에 로드 코드가 없는 경우

defaults.xml만 있고 Java에서 안 읽는 BSP는 드물지만, 없다면 추가가 필요합니다.
`loadSecureSettings` 부근 예:

```java
loadStringSetting(stmt, Settings.Secure.DEFAULT_INPUT_METHOD,
        R.string.def_default_input_method);
loadStringSetting(stmt, Settings.Secure.ENABLED_INPUT_METHODS,
        R.string.def_enabled_input_methods);
```

리소스 이름은 자기 트리에 맞게.  
가능하면 **framework 수정 대신 기존 벤더 경로를 재사용**하세요.

---

## 6. 빌드 · 플래시

```bash
source build/envsetup.sh
lunch ceres_c3-userdebug   # 실제 타겟명

m SettingsProvider HGKeyboard
# 또는
m -j$(nproc)
# + 벤더 pack 스크립트
```

플래시 후 **userdata가 유지되면 예전 기본 IME가 남을 수 있음**  
→ 출하 검증 시 **factory reset** 권장.

---

## 7. 검증

```bash
adb shell pm path com.higenis.keyboard
adb shell ime list -s
adb shell settings get secure default_input_method
adb shell settings get secure enabled_input_methods
```

기대 예:

```text
default_input_method =
  com.higenis.keyboard/.HGKeyboardService

enabled_input_methods =
  com.higenis.keyboard/.HGKeyboardService:com.android.inputmethod.latin/.LatinIME
```

EditText 포커스 시 HG Keyboard가 바로 뜨면 성공입니다.

---

## 8. 개발 중 임시 변경 (이미지 재빌드 없이)

```bash
adb shell ime enable com.higenis.keyboard/.HGKeyboardService
adb shell ime set com.higenis.keyboard/.HGKeyboardService
# 또는
adb shell settings put secure default_input_method com.higenis.keyboard/.HGKeyboardService
```

이것은 **런타임만** 바뀌며, 공장 초기화하면 defaults.xml 값으로 돌아갑니다.

---

## 9. 체크리스트

- [ ] `HGKeyboard`가 system 이미지에 포함 (`pm path` → `/system/app/...`)
- [ ] BSP의 `def_*input_method*` 리소스 이름 확인
- [ ] overlay 또는 softwinner defaults.xml 수정
- [ ] SettingsProvider가 해당 string을 로드함
- [ ] full image 빌드 + 플래시
- [ ] factory reset (또는 신규 userdata)
- [ ] `settings get secure default_input_method` 확인

---

## 10. 하지 말아야 할 것

- SetupWizard / framework IME 강제 핵을 먼저 쓰기 (유지보수 어려움)
- `PRODUCT_PROPERTY_OVERRIDES`에 임의 persist 키를 넣는 방식 (표준 AOSP 아님, 벤더가 읽도록 패치된 경우만 유효)
- `testapp`을 PRODUCT_PACKAGES에 넣기

---

## 관련 파일 (이 저장소)

| 파일 | 용도 |
|------|------|
| `aosp/product/hgkeyboard.mk` | 패키지만 포함 |
| `aosp/product/hgkeyboard_default_ime.mk` | 패키지 + default IME overlay |
| `aosp/overlay/.../defaults.xml` | SettingsProvider 기본값 예시 |

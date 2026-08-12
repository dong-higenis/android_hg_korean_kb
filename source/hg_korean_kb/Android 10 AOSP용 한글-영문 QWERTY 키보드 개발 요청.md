# Android 10 AOSP용 한글/영문 QWERTY 키보드 개발 요청

Android 10(AOSP, API 29) 기반 기기에 기본 탑재할 수 있는 한글/영문 소프트웨어 키보드(IME)를 개발해줘.

최종 목표는 일반 APK를 별도로 설치하는 형태가 아니라, Android 10 AOSP 소스에 프로젝트를 포함하여 시스템 이미지와 함께 빌드 및 배포하는 것이다.

## 1. 개발 환경 및 기본 조건

- Target OS: Android 10 / AOSP 10
- API Level: 29
- AOSP 빌드 시스템에 포함할 수 있어야 함
- 가능하면 AOSP 기본 빌드 시스템(Android.bp 또는 Android.mk)을 이용
- 시스템 앱 또는 시스템 IME로 탑재 가능하도록 구성
- 외부 라이브러리 의존성을 최소화할 것
- 인터넷 연결 없이 사용할 수 있어야 함
- ARM / ARM64 Android 장치에서 동작 가능해야 함

프로젝트 예시 경로:

packages/inputmethods/HGKeyboard/

형태로 AOSP source tree 내부에 추가할 수 있도록 구성해줘.

---

# 2. 키보드 기본 기능

한국어와 영어 입력을 모두 지원하는 QWERTY 키보드를 구현해줘.

지원 언어:

- 한국어
- 영어

반드시 다음 키를 포함한다.

- 한/영 전환
- Shift
- Backspace
- Enter
- Space
- 숫자/특수문자 전환
- 쉼표
- 마침표

키보드의 기본적인 배치는 Google Gboard의 일반적인 QWERTY 키보드와 비슷한 사용성을 갖도록 해줘.

단, Google의 이미지, 아이콘, 코드, 리소스 등을 복제하지 말고 일반적인 Android QWERTY 키보드 레이아웃과 Material 스타일을 참고하여 독자적으로 구현한다.

---

# 3. 영문 키보드

기본 영문 배열은 다음과 같다.

Q W E R T Y U I O P

A S D F G H J K L

Shift
Z X C V B N M
Backspace

하단:

?123
한/영
,
Space
.
Enter

Shift 상태에 따라

a → A

처럼 대문자/소문자가 변경되어야 한다.

Shift 동작:

- 1회 누름: 다음 문자 대문자
- 가능하면 Caps Lock 지원
- Shift 두 번 누르면 Caps Lock 활성화 방식 사용 가능

---

# 4. 한글 키보드

한국 표준 두벌식 QWERTY 배열을 사용한다.

기본 자판:

ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ

ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ

Shift
ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ
Backspace

Shift 입력:

ㅂ → ㅃ
ㅈ → ㅉ
ㄷ → ㄸ
ㄱ → ㄲ
ㅅ → ㅆ
ㅐ → ㅒ
ㅔ → ㅖ

등 일반적인 한국어 두벌식 키보드와 동일하게 동작하도록 한다.

---

# 5. 한글 조합 기능

이 부분이 가장 중요하다.

단순히

ㅎ
ㅏ
ㄴ

을 각각 입력하는 것이 아니라

"한"

으로 조합되어야 한다.

한국어 입력기는 Unicode Hangul composition 규칙을 구현해야 한다.

초성 + 중성 + 종성 조합을 지원한다.

예:

ㅎ + ㅏ + ㄴ
→ 한

ㄱ + ㅡ + ㄹ
→ 글

ㅎ + ㅏ + ㄴ + ㄱ + ㅡ + ㄹ
→ 한글

다음과 같은 복합 모음도 지원한다.

ㅗ + ㅏ → ㅘ
ㅗ + ㅐ → ㅙ
ㅗ + ㅣ → ㅚ
ㅜ + ㅓ → ㅝ
ㅜ + ㅔ → ㅞ
ㅜ + ㅣ → ㅟ
ㅡ + ㅣ → ㅢ

복합 종성도 지원한다.

ㄱ + ㅅ → ㄳ
ㄴ + ㅈ → ㄵ
ㄴ + ㅎ → ㄶ
ㄹ + ㄱ → ㄺ
ㄹ + ㅁ → ㄻ
ㄹ + ㅂ → ㄼ
ㄹ + ㅅ → ㄽ
ㄹ + ㅌ → ㄾ
ㄹ + ㅍ → ㄿ
ㄹ + ㅎ → ㅀ
ㅂ + ㅅ → ㅄ

Backspace를 누르면 한글 조합 상태를 역순으로 분리해야 한다.

예:

괜
→ 괘
→ 과
→ 고
→ ㄱ
→ 삭제

와 같이 현재 조합 중인 Hangul syllable을 단계적으로 분해하는 동작을 구현한다.

한글 조합 로직은 UI 코드와 분리하여 별도의 HangulComposer 클래스로 작성해줘.

예:

HangulComposer.java

또는

HangulComposer.kt

내부에서 한글 상태 머신을 관리하도록 한다.

---

# 6. 한/영 전환

하단에

한/영

키를 배치한다.

누르면:

한국어 → 영어
영어 → 한국어

로 즉시 변경되어야 한다.

현재 언어 상태에 따라 키캡의 문자가 변경되어야 한다.

한국어:

ㅂ ㅈ ㄷ ㄱ ...

영어:

Q W E R T ...

IME가 닫혔다 다시 열리더라도 마지막으로 선택한 한/영 상태를 유지할 수 있도록 SharedPreferences 등을 사용한다.

---

# 7. 숫자 및 특수문자

?123 버튼을 누르면 숫자/특수문자 키보드로 전환한다.

최소 다음 문자를 지원한다.

1 2 3 4 5 6 7 8 9 0

- / : ; ( ) ₩ & @ "

추가 특수문자 화면도 구성할 수 있도록 한다.

ABC 버튼을 누르면 기존 한글 또는 영어 QWERTY 화면으로 돌아온다.

---

# 8. UI 디자인

전체적인 키보드 스타일은 최신 Google Gboard와 유사한 느낌의 깔끔한 Android 키보드 UI로 만든다.

단, Google의 디자인 자산을 그대로 복사하지 말고 일반적인 Material Design 스타일로 구현한다.

디자인 요구사항:

- 키 사이 간격이 존재할 것
- 둥근 사각형 형태의 키
- 가독성 높은 글자
- 충분한 터치 영역
- Key press 시 시각적인 feedback 제공
- Shift / Backspace / Enter 등의 특수키는 일반 문자 키와 구분
- 화면 너비에 맞게 자동으로 키 크기 조절
- 다양한 LCD 해상도를 고려
- portrait 화면 우선 지원
- dp / sp 단위를 사용할 것

가능하면 키보드 높이와 key 크기를 하드코딩하지 말고 화면 크기에 따라 유연하게 대응하도록 한다.

---

# 9. 아이콘

다음 특수키에는 가능하면 텍스트 대신 간단한 vector drawable 아이콘을 사용한다.

- Shift
- Backspace
- Enter

아이콘은 직접 제작하거나 Android Material 스타일의 일반적인 아이콘 형태로 구성하며 Google Gboard 자산을 직접 복사하지 않는다.

---

# 10. Android IME 구조

Android InputMethodService를 기반으로 구현한다.

예:

HGKeyboardService
extends InputMethodService

필요한 주요 구성:

AndroidManifest.xml

HGKeyboardService.java 또는 .kt

Keyboard UI

Keyboard Layout XML 또는 Custom View

HangulComposer

Language state

Special key handling

InputConnection 처리

onCreateInputView()

onStartInputView()

commitText()

setComposingText()

finishComposingText()

deleteSurroundingText()

등 Android IME API를 적절하게 사용한다.

특히 한글 입력 중에는 commitText만 반복 사용하지 말고 setComposingText()를 이용하여 조합 중인 문자를 표시하도록 구현한다.

예:

ㅎ
→ 하
→ 한

이 입력되는 동안 composing state를 유지한다.

다음 글자로 넘어가거나 Space/Enter를 누르면 조합을 확정(commit)한다.

---

# 11. 입력 필드 대응

EditorInfo.inputType을 확인해서 입력 필드에 따라 키보드 종류를 변경할 수 있도록 기본 구조를 만들어줘.

예:

일반 텍스트
→ 한글/영문 QWERTY

숫자 입력 필드
→ 숫자 키패드

Password
→ 자동완성/조합 관련 문제가 발생하지 않도록 안전하게 처리

Email
→ @, . 키 접근성을 높일 수 있는 구조

URL
→ /, . 등을 쉽게 입력할 수 있도록 확장 가능하게 구성

최초 버전에서는 일반 Text와 Number를 우선 구현하고, 나머지는 확장 가능한 형태로 설계해도 된다.

---

# 12. Long Press

Backspace 버튼은 길게 누르면 반복 삭제되도록 구현한다.

예:

짧게 터치
→ 한 단계 삭제

길게 터치
→ 일정 시간 후 연속 삭제

초기 delay 약 400~500ms

반복 간격 약 50~100ms

정도로 구현하되 상수로 만들어 쉽게 변경할 수 있도록 한다.

---

# 13. 진동 및 키 피드백

가능하면 key press 시 Android의 기본 HapticFeedback을 사용할 수 있도록 구현한다.

하지만 설정에서 쉽게 On/Off 할 수 있는 구조로 만들어줘.

Sound 역시 추후 추가할 수 있도록 구조만 고려한다.

---

# 14. AOSP 통합

최종적으로 이 키보드를 Android 10 AOSP build에 포함시키는 방법까지 작성해줘.

예:

packages/inputmethods/HGKeyboard

에 소스를 추가하고

Android.bp

또는 Android.mk

를 작성한다.

product makefile에

PRODUCT_PACKAGES += HGKeyboard

형태로 추가할 수 있도록 한다.

시스템 이미지 빌드 후

/system/app/

또는

/system/priv-app/

등 적절한 위치에 포함되도록 구성한다.

IME이 Android Settings의

Settings
→ System
→ Languages & input
→ Virtual keyboard

에서 선택 가능해야 한다.

---

# 15. 기본 키보드 지정 방법

우리는 특정 Android 기기에 이 OS를 탑재해서 제품으로 출하할 예정이다.

따라서 사용자가 처음 부팅했을 때 HGKeyboard를 사용할 수 있도록 하는 방법도 설명해줘.

AOSP SettingsProvider / secure settings / overlay 등을 이용해서

enabled_input_methods

default_input_method

관련 설정을 어떻게 처리하는 것이 적절한지 Android 10 기준으로 설명한다.

가능하면 AOSP에서 기본 IME로 설정하는 권장 방법을 제시해줘.

다만 Android 보안 정책이나 초기 설정 과정에 영향을 줄 수 있는 부분은 임의로 우회하지 말고 AOSP framework 수준에서 정상적인 방법으로 구현한다.

---

# 16. 패키지 정보

예시:

Application Name:
HG Keyboard

Package:
com.higenis.keyboard

IME Service:
com.higenis.keyboard.HGKeyboardService

IME ID가 실제 Android에서 어떤 형식으로 생성되는지도 설명해줘.

---

# 17. 코드 작성 원칙

코드는 다음 조건을 지켜줘.

1. Android 10에서 정상적으로 컴파일되어야 한다.
2. Android 10 이후 버전에서 추가된 API를 무조건 사용하지 않는다.
3. deprecated API를 사용할 경우 Android 10에서 필요한 이유를 설명한다.
4. 한글 조합 엔진과 UI를 분리한다.
5. 각 클래스의 역할을 명확하게 나눈다.
6. 중요한 로직에는 주석을 작성한다.
7. magic number를 최소화한다.
8. 키코드 및 상태 값은 enum/constant로 관리한다.
9. 유지보수가 쉽도록 구성한다.
10. 외부 라이브러리에 의존하지 않는다.

---

# 18. 테스트 항목

다음 입력이 정확하게 되는지 테스트 코드를 만들거나 테스트 절차를 작성한다.

한글:

가
각
간
값
괜
대한민국
안녕하세요
하이제니스
한글키보드

특히 다음 입력 흐름을 확인한다.

ㅎ ㅏ ㄴ ㄱ ㅡ ㄹ
→ 한글

ㄱ ㅏ ㅂ ㅅ
→ 값

ㄱ ㅗ ㅐ ㄴ
→ 괜

Backspace 조합 해제 테스트도 반드시 포함한다.

영문:

hello
Hello
ANDROID
Android

한/영 혼합:

Android 한글 Keyboard
ESP32 개발보드
STM32 테스트

---

# 19. 프로젝트 구조

먼저 전체 프로젝트 구조를 제안해줘.

예:

HGKeyboard/
├── Android.bp
├── AndroidManifest.xml
├── res/
│   ├── drawable/
│   ├── layout/
│   ├── values/
│   └── xml/
└── src/com/higenis/keyboard/
    ├── HGKeyboardService.java
    ├── KeyboardView.java
    ├── KeyboardController.java
    ├── HangulComposer.java
    ├── KeyboardState.java
    └── KeyModel.java

필요하다면 더 적절한 구조로 변경해도 된다.

---

# 20. 개발 진행 순서

한 번에 모든 코드를 무작정 생성하지 말고 다음 순서대로 작업해줘.

STEP 1
Android 10 AOSP IME 구조 분석 및 프로젝트 구조 제안

STEP 2
AndroidManifest.xml / Android.bp 작성

STEP 3
기본 InputMethodService 구현

STEP 4
QWERTY Keyboard UI 구현

STEP 5
영문 입력 구현

STEP 6
한/영 전환 구현

STEP 7
HangulComposer 구현

STEP 8
복합 모음 / 복합 종성 구현

STEP 9
Backspace 한글 조합 해제 구현

STEP 10
숫자 및 특수문자 키보드

STEP 11
EditorInfo.inputType 대응

STEP 12
AOSP 빌드 및 Product 설정

STEP 13
기본 IME 설정 방법

STEP 14
테스트 및 버그 수정

각 STEP마다 실제 컴파일 가능한 전체 파일 단위 코드를 제시하고, 기존 파일을 수정해야 한다면 어느 부분을 수정해야 하는지 명확하게 알려줘.

내가 "다음"이라고 하면 다음 STEP으로 진행하는 방식으로 작업한다.

가장 먼저 STEP 1부터 시작해줘.
# HG Keyboard — system package + default IME (STEP 13)
#
# In device/softwinner/<board>/device.mk (or product .mk), add ONCE:
#
#   $(call inherit-product, packages/inputmethods/HGKeyboard/aosp/product/hgkeyboard_default_ime.mk)
#
# Do NOT also inherit hgkeyboard.mk (would duplicate PRODUCT_PACKAGES).
#
# Prerequisites:
#   1) packages/inputmethods/HGKeyboard sources present
#   2) Overlay resource names match THIS BSP's SettingsProvider
#      (see aosp/overlay/.../defaults.xml comments)
#   3) After flash, factory reset / wipe data if settings DB already exists

PRODUCT_PACKAGES += \
    HGKeyboard

# Runtime defaults for Settings.Secure.DEFAULT_INPUT_METHOD /
# ENABLED_INPUT_METHODS via SettingsProvider resource overlay.
PRODUCT_PACKAGE_OVERLAYS += \
    packages/inputmethods/HGKeyboard/aosp/overlay

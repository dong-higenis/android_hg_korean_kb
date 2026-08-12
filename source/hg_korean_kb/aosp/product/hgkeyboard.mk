# HG Keyboard — include in a product makefile once (do not duplicate).
#
# Copy this fragment into the product's device.mk / <product>.mk, or:
#   $(call inherit-product, packages/inputmethods/HGKeyboard/aosp/product/hgkeyboard.mk)
#
# Prefer one PRODUCT_PACKAGES site in the inherit-product chain that uniquely
# owns this device's app set. Do not add the same line in both device.mk and
# a parent product.mk.

PRODUCT_PACKAGES += \
    HGKeyboard

# Package inclusion only.
# For factory default IME, inherit hgkeyboard_default_ime.mk instead
# (see AOSP_DEFAULT_IME.md). Do not inherit both.

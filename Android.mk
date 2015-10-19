LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

appcompat_dir := ../../../prebuilts/sdk/current/support/v7/appcompat/res
res_dir := res $(appcompat_dir)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES += \
    android-support-v13 \
    android-support-v4 \
    android-support-v7-appcompat \
    org.apache.http.legacy

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dir))
LOCAL_SRC_FILES := $(call all-java-files-under, src/)

ifneq ($(BOARD_DEVICE_SETTINGS),)
    LOCAL_RESOURCE_DIR += $(BOARD_DEVICE_SETTINGS)/res
    LOCAL_SRC_FILES += $(call all-java-files-under, ../../../$(BOARD_DEVICE_SETTINGS)/src)
endif

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat

LOCAL_PACKAGE_NAME := EVToolbox
LOCAL_CERTIFICATE := platform

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

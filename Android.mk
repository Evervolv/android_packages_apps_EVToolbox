LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v13 android-support-v4



ifneq ($(BOARD_DEVICE_SETTINGS),)
    LOCAL_RESOURCE_DIR += $(BOARD_DEVICE_SETTINGS)/res
    LOCAL_SRC_FILES += $(call all-java-files-under, ../../../$(BOARD_DEVICE_SETTINGS)/src)
endif

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res
LOCAL_SRC_FILES += $(call all-java-files-under, src)

LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_PACKAGE_NAME := EVToolbox
LOCAL_CERTIFICATE := platform

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

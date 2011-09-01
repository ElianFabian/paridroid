LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := paridroid
LOCAL_SRC_FILES := libpari.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := paridroid-helper
LOCAL_SRC_FILES := paridroid_helper.c
include $(BUILD_SHARED_LIBRARY)
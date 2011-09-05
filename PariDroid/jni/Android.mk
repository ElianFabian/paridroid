LOCAL_PATH := $(call my-dir)

## Commented out because we are currently just using the statically compiled gp binary.
## 09/01/2011

##include $(CLEAR_VARS)
##LOCAL_MODULE := paridroid
##LOCAL_SRC_FILES := libpari.so
##include $(PREBUILT_SHARED_LIBRARY)

## This currently just provides the installer.
## 09/01/2011

include $(CLEAR_VARS)
LOCAL_MODULE := paridroid-helper
LOCAL_SRC_FILES := \
	paridroid_helper.c \
	paridroid_install.c \
	log.c
LOCAL_C_INCLUDES := \
	paridroid_install.h \
	log.h
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
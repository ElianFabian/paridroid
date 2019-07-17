LOCAL_PATH := $(call my-dir)

############################
# libpari.so
############################

include $(CLEAR_VARS)

LOCAL_MODULE := pari

LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/pari/lib/libpari.so

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/pari/include

include $(PREBUILT_SHARED_LIBRARY)

############################
# libparidroid.so
############################

include $(CLEAR_VARS)

LOCAL_MODULE := paridroid

LOCAL_LDLIBS := -llog

LOCAL_CFLAGS := -g

LOCAL_SRC_FILES := \
	paridroid/paridroid.c \
	paridroid/fr_ubordeaux_math_paridroid_PariNative.c

LOCAL_SHARED_LIBRARIES := libpari

include $(BUILD_SHARED_LIBRARY)

# EOF

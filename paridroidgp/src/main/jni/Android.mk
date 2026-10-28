LOCAL_PATH := $(call my-dir)

############################
# libgmp.so
############################

include $(CLEAR_VARS)

LOCAL_MODULE := gmp

LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/gmp/lib/libgmp.so

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/$(TARGET_ARCH_ABI)/gmp/include

include $(PREBUILT_SHARED_LIBRARY)

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

LOCAL_CFLAGS := -g -std=gnu99 -Wno-undefined-internal -DINLINE="static __inline__"

LOCAL_LDFLAGS := -Wl,-z,max-page-size=16384

LOCAL_SRC_FILES := \
	paridroid/paridroid.c \
	paridroid/fr_ubordeaux_math_paridroid_PariNative.c

LOCAL_SHARED_LIBRARIES := pari gmp

include $(BUILD_SHARED_LIBRARY)

# EOF

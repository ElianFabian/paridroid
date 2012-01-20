LOCAL_PATH := $(call my-dir)

############################
# libpari-gmp.so
############################

include $(CLEAR_VARS)

LOCAL_MODULE := pari-gmp

LOCAL_SRC_FILES := pari/lib/libpari-gmp.so

include $(PREBUILT_SHARED_LIBRARY)

############################
# libparidroid.so
############################

include $(CLEAR_VARS)

LOCAL_MODULE := paridroid

LOCAL_C_INCLUDES := pari/include

LOCAL_SRC_FILES := \
	paridroid/paridroid.c \
	paridroid/com_math_paridroid_PariNative.c

LOCAL_SHARED_LIBRARIES := libpari-gmp

include $(BUILD_SHARED_LIBRARY)

# EOF
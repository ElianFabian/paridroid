LOCAL_PATH := $(call my-dir)

############################
# LIBPARI-GMP.SO
# (PREBUILT)
############################

include $(CLEAR_VARS)

LOCAL_MODULE := pari-gmp

LOCAL_SRC_FILES := pari/lib/libpari-gmp.so

include $(PREBUILT_SHARED_LIBRARY)

############################
# LIBPARIDROID
############################

include $(CLEAR_VARS)

LOCAL_MODULE := paridroid

LOCAL_C_INCLUDES := pari/include

LOCAL_SRC_FILES := paridroid/paridroid.c

LOCAL_SHARED_LIBRARIES := libpari-gmp

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)

# EOF


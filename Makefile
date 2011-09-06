.PHONY: paridroid gp-sta

LOCAL_DIR := $(shell pwd)

NDK_VERSION := r6b
NDK := /opt/android-ndk-$(NDK_VERSION)
NDK_TOOLCHAIN := /tmp/android-toolchain

PARI_VERSION := 2.3.5
PARI_DIR := $(LOCAL_DIR)/pari/pari-$(PARI_VERSION)
PARI_CFG := $(PARI_DIR)/pari.cfg

READLINE_VERSION := 6.2
READLINE_DIR := $(LOCAL_DIR)/readline/readline-$(READLINE_VERSION)
READLINE_CFG := $(READLINE_DIR)/androidcfg.sh

PARIDROID_DIR := $(LOCAL_DIR)/PariDroid
PARIDROID_JNI := $(PARIDROID_DIR)/jni

$(PARI_DIR)/obj-dyn/Makefile:
	cd $(PARI_DIR)
	$(PARI_DIR)/Configure -l $(PARI_CFG)

gp-sta: $(PARI_DIR)/obj-dyn/Makefile
	cd $(PARI_DIR)/obj-dyn
	make gp-sta

$(PARIDROID_DIR)/assets/gp.jpg: gp-sta
	cp $(PARI_DIR)/obj-dyn/gp-sta $(PARIDROID_DIR)/assets/gp.jpg

$(PARIDROID_DIR)/libs/armeabi/libparidroid-helper.so:
	cd $(PARIDROID_DIR)
	$(NDK)/ndk-build V=1

$(PARIDROID_DIR)/bin/PariDroid-debug.apk: $(PARIDROID_DIR)/assets/gp.jpg $(PARIDROID_DIR)/libs/armeabi/libparidroid-helper.so
	ant debug -buildfile $(PARIDROID_DIR)/build.xml

PariDroid.apk: $(PARIDROID_DIR)/bin/PariDroid-debug.apk
	mv $(PARIDROID_DIR)/bin/PariDroid-debug.apk $(LOCAL_DIR)/PariDroid.apk

paridroid: PariDroid.apk
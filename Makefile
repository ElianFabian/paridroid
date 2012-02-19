
TOP:=$(shell pwd)
ANDROID:=$(TOP)/android

PARIDROID_ROOT:=$(TOP)/PariDroid
PARIDROID_JNI:=$(PARIDROID_ROOT)/jni

FETCH_PARI:=$(PARI_ROOT)/fetch_pari_src.sh
NDK_BUILD:=$(ANDROID)/ndk/ndk-build

all: all-debug

all-debug:
	$(NDK_BUILD) -C $(PARIDROID_JNI) -B V=1
	ant -buildfile $(PARIDROID_ROOT)/build.xml debug
	cp $(PARIDROID_ROOT)/bin/PariDroid-debug.apk PariDroid.apk

clean: paridroid_clean ndk_clean
	rm -f PariDroid.apk

ndk_clean:
	$(NDK_BUILD) -C $(PARIDROID_JNI) clean

paridroid_clean:
	cd $(PARIDROID_ROOT)
	ant -buildfile $(PARIDROID_ROOT)/build.xml clean

install:
	$(ANDROID)/sdk/platform-tools/adb install -r PariDroid.apk

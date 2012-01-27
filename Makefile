
TOP:=$(shell pwd)
ANDROID:=$(TOP)/android

PARIDROID_ROOT:=$(TOP)/PariDroid
PARIDROID_JNI:=$(PARIDROID_ROOT)/jni

FETCH_PARI:=$(PARI_ROOT)/fetch_pari_src.sh
NDK_BUILD:=$(ANDROID)/ndk/ndk-build

all: PariDroid.apk

$(PARIDROID_ROOT)/libs/armeabi/libparidroid.so:
	$(NDK_BUILD) -C $(PARIDROID_JNI) -B V=1

$(PARIDROID_ROOT)/bin/PariDroid-debug.apk: libparidroid
	ant -buildfile $(PARIDROID_ROOT)/build.xml debug

libparidroid: $(PARIDROID_ROOT)/libs/armeabi/libparidroid.so

PariDroid.apk: $(PARIDROID_ROOT)/bin/PariDroid-debug.apk
	cp $< $@

clean: paridroid_clean ndk_clean
	rm PariDroid.apk

ndk_clean:
	$(NDK_BUILD) -C $(PARIDROID_JNI) clean

paridroid_clean:
	cd $(PARIDROID_ROOT)
	ant -buildfile $(PARIDROID_ROOT)/build.xml clean

install:
	$(ANDROID)/sdk/platform-tools/adb install -r PariDroid.apk

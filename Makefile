# This Makefile uses the environment variable $(NDK), which needs to
# contain the path to the Android NDK. Please set it in your environment
# before invoking make.

.PHONY: all apk paridroid libpari libpari-config libpari-make check clean dist-clean

NDK_PLATFORM=android-9
GCC_VERSION=4.8
SYSROOT=$(NDK)/platforms/$(NDK_PLATFORM)/arch-arm
CC="$(NDK)/toolchains/arm-linux-androideabi-$(GCC_VERSION)/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc --sysroot=$(SYSROOT)"

PARIDROID_VERSION=$(shell grep versionName PariDroid/AndroidManifest.xml | grep -o '".*"' | tr -d '"')
PARI_SRC=pari
PARIDROID_SRC=$(CURDIR)/PariDroid
JNI=$(PARIDROID_SRC)/jni
PARI_PREFIX=$(JNI)/pari

all: libpari paridroid apk

apk: paridroid
	cp $(PARIDROID_SRC)/bin/PariDroid-debug.apk PariDroid-$(PARIDROID_VERSION).apk

paridroid: $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h
	cd $(JNI) && $(NDK)/ndk-build APP_CFLAGS="-DPARIDROID_VERSION=$(PARIDROID_VERSION)" V=1
	cd $(PARIDROID_SRC) && ant debug

$(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h: libpari
	cd $(PARIDROID_SRC) && ant debug
	cd $(PARIDROID_SRC)/bin/classes && javah -jni fr.ubordeaux.math.paridroid.PariNative
	mv $(PARIDROID_SRC)/bin/classes/fr_ubordeaux_math_paridroid_PariNative.h $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h

libpari: libpari-config libpari-make

libpari-config:
	cd $(PARI_SRC) && \
	CC=$(CC) RUNTEST="../runtest" ./Configure --host=arm-linux --builddir=Oandroid-arm --kernel=arm-none --without-readline --graphic=none --prefix=$(PARI_PREFIX) --datadir=/data/data/fr.ubordeaux.math.paridroid

libpari-make:
	cd $(PARI_SRC)/Oandroid-arm && \
	$(MAKE) all && \
	make install-include install-lib-dyn LIBPARI_SONAME=libpari.so

check: libpari
	cd $(PARI_SRC)/Oandroid-arm && \
	adb push gp-sta /data && \
	make statest-all RUNTEST=../../runtest.bench && \
	adb shell rm /data/gp-sta /data/pari-ret /data/pari-stdin

clean:
	rm -f PariDroid*.apk
	cd $(PARIDROID_SRC) && ant clean

distclean: clean
	cd $(JNI) && $(NDK)/ndk-build clean V=1
	rm -rf $(PARI_PREFIX)
	rm -rf $(PARI_SRC)/android
	rm -rf $(PARI_SRC)/Oandroid-arm


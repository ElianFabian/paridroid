# This Makefile uses the environment variable $(NDK), which needs to
# contain the path to the Android NDK. Please set it in your environment
# before invoking make.

.PHONY: all apk paridroid pari pari-config pari-make pari-real-config check clean dist-clean

NDK_PLATFORM=android-9
GCC_VERSION=4.8
SYSROOT=$(NDK)/platforms/$(NDK_PLATFORM)/arch-arm
CC="$(NDK)/toolchains/arm-linux-androideabi-$(GCC_VERSION)/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc --sysroot=$(SYSROOT)"

PARIDROID_VERSION=$(shell grep versionName PariDroid/AndroidManifest.xml | grep -o '".*"' | tr -d '"')
PARI_VERSION=$(shell echo $(PARIDROID_VERSION) | cut -d. -f1-3)
ifeq "$(PARI_VERSION)" "2.8.0"
   PARI_GIT="master"
else
   PARI_GIT=pari-$(PARI_VERSION)
endif

TOP=$(CURDIR)
PARIDROID_SRC=$(TOP)/PariDroid
PARI_SRC=$(TOP)/srclib/pari
JNI=$(PARIDROID_SRC)/jni

all: apk

apk: paridroid
	cp $(PARIDROID_SRC)/bin/PariDroid-debug.apk PariDroid-$(PARIDROID_VERSION).apk

paridroid: pari $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h
	cd $(JNI) && $(NDK)/ndk-build APP_CFLAGS="-DPARIDROID_VERSION=$(PARIDROID_VERSION)" V=1
	cd $(PARIDROID_SRC) && ant debug

pari: pari-make

pari-config: $(PARI_SRC)
	rm -rf $(PARI_SRC)/Oandroid-arm
	cd $(PARI_SRC) && git checkout $(PARI_GIT)
	cd $(PARI_SRC) && \
	./Configure -l $(TOP)/config/$(PARI_VERSION)/pari.cfg

pari-make: pari-config
	cd $(PARI_SRC)/Oandroid-arm && \
	$(MAKE) CC=$(CC) all && \
	make install-include install-lib-dyn LIBPARI_SONAME=libpari.so DESTDIR=$(JNI)/pari


# This is supposed to be executed only once; subsequent updates need to be
# executed via "git pull" by hand, so that the Makefile remains usable
# offline.
$(PARI_SRC):
	mkdir -p $(PARI_SRC)
	git clone http://pari.math.u-bordeaux.fr/git/pari.git $(PARI_SRC)


# The following targets create files that ultimately are supposed to be under
# version control, but that can be generated automatically.

# Create a C header file from the java class interfacing with the pari
# library; this gives the prototypes of functions to be written by hand in
# the corresponding C source file. The result is under version control.
$(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h: $(PARIDROID_SRC)/src/fr/ubordeaux/math/paridroid/PariNative.java
	cd $(PARIDROID_SRC) && ant debug
	cd $(PARIDROID_SRC)/bin/classes && javah -jni fr.ubordeaux.math.paridroid.PariNative
	mv $(PARIDROID_SRC)/bin/classes/fr_ubordeaux_math_paridroid_PariNative.h $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h

# Create a pari configuration by running the tests on an Android emulator.
# Such an emulator is usually not available in build environments, so the
# resulting configuration files, which depend on the pari version, need to
# be moved to the config/ subdirectory, from where they are copied when
# compiling a pari library.
pari-real-config:
	rm -rf $(PARI_SRC)/Oandroid-arm
	cd $(PARI_SRC) && \
	CC=$(CC) RUNTEST="../../runtest" ./Configure --host=arm-linux --builddir=Oandroid-arm --kernel=arm-none --without-readline --graphic=none --prefix=/ --datadir=/data/data/fr.ubordeaux.math.paridroid && \
	sed -i 's/^TOP=.*/TOP=../' Oandroid-arm/pari.cfg && \
	sed -i 's/^RUNTEST=.*/RUNTEST=/' Oandroid-arm/pari.cfg && \
	sed -i 's/^prefix=.*/prefix=/' Oandroid-arm/pari.cfg


check: pari
	cd $(PARI_SRC)/Oandroid-arm && \
	adb push gp-sta /data && \
	make statest-all RUNTEST=../../runtest.bench && \
	adb shell rm /data/gp-sta /data/pari-ret /data/pari-stdin

clean:
	rm -f PariDroid*.apk
	cd $(PARIDROID_SRC) && ant clean
	cd $(JNI) && $(NDK)/ndk-build clean V=1
	rm -rf $(JNI)/pari

distclean: clean
	rm -rf $(PARI_SRC)/Oandroid-arm
	rm -f $(PARI_SRC)/config-arm-linux*
	rm -f $(PARI_SRC)/config/linux-arm*


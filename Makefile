# This Makefile uses the environment variable $(NDK), which needs to
# contain the path to the Android NDK. Please set it in your environment
# before invoking make.

.PHONY: all apk apk-fast pari pari-standalone pari-checkout pari-config-standalone config pari-config pari-make pari-check check check-standalone clean distclean

NDK_PLATFORM=android-9
GCC_VERSION=4.9
SYSROOT=$(NDK)/platforms/$(NDK_PLATFORM)/arch-arm
CC='$(NDK)/toolchains/arm-linux-androideabi-$(GCC_VERSION)/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc --sysroot=$(SYSROOT)'

PARIDROID_VERSION=$(shell grep versionName PariDroid/AndroidManifest.xml | grep -o '".*"' | tr -d '"')
PARI_VERSION=$(shell echo $(PARIDROID_VERSION) | cut -d. -f1-3)
ifeq "$(PARI_VERSION)" "2.8.0"
   PARI_GIT=origin/master
else
   PARI_GIT=pari-$(PARI_VERSION)
endif

TOP=$(CURDIR)
PARIDROID_SRC=$(TOP)/PariDroid
PARI_SRC=$(TOP)/../srclib/pari
JNI=$(PARIDROID_SRC)/jni
TARGET=Oandroid-arm

all: apk

apk: pari apk-fast

apk-fast: $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h
	cd $(PARIDROID_SRC) && $(NDK)/ndk-build V=1
	cd $(PARIDROID_SRC) && ant release
	cp $(PARIDROID_SRC)/bin/PariDroid-release-unsigned.apk $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $(HOME)/.android/debug.keystore -storepass android $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk androiddebugkey
	zipalign -f 4 $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk PariDroid-$(PARIDROID_VERSION).apk

# Use the pari version specified in AndroidManifest.xml.
pari: pari-checkout pari-config pari-make

# Use the pari version that happens to be in the source tree; useful for
# manual checking of pari versions independently of the Android manifest.
pari-standalone: pari-config-standalone pari-make

# Updates the pari source tree and switches to the version from
# AndroidManifest.xml
pari-checkout: $(PARI_SRC)
	cd $(PARI_SRC) && git fetch && git checkout $(PARI_GIT)

# Create a pari configuration by running the tests on an Android emulator.
# Such an emulator is usually not available in build environments, so the
# resulting configuration files, which depend on the pari version, need to
# be moved to the config/ subdirectory, from where they are copied when
# compiling a pari library.
# The calls to sed eliminate all references to files on the configuring machine
# and the option -Wall to the linker.
pari-config-standalone:
	rm -rf $(PARI_SRC)/$(TARGET)
	cd $(PARI_SRC) && \
	CC=$(CC) RUNTEST=$(TOP)/runtest ./Configure --host=arm-linux --builddir=$(TARGET) --kernel=arm-none --without-readline --graphic=none --prefix=/ --datadir=/data/data/fr.ubordeaux.math.paridroid && \
	sed -i 's/^TOP=.*/TOP=../' $(TARGET)/pari.cfg && \
	sed -i 's/^RUNTEST=.*/RUNTEST=/' $(TARGET)/pari.cfg && \
	sed -i 's/^CC=.*/CC=XXXCCXXX/' $(TARGET)/pari.cfg && \
	sed -i 's/^LD=.*/LD=XXXCCXXX/' $(TARGET)/pari.cfg && \
	sed -i 's/^LDused=.*/LDused=/' $(TARGET)/pari.cfg && \
	sed -i 's/^DLLD=.*/DLLD=XXXCCXXX/' $(TARGET)/pari.cfg && \
	sed -i 's/^MODLD=.*/MODLD=/' $(TARGET)/pari.cfg && \
	sed -i 's/^modules_build=.*/modules_build=/' $(TARGET)/pari.cfg && \
	sed -i 's/^prefix=.*/prefix=/' $(TARGET)/pari.cfg
#	sed -i "s/^LDFLAGS='-O3\ -Wall/LDFLAGS='-O3/" $(TARGET)/pari.cfg

# Configure pari using the pari.cfg taken from the appropriate config/
# subdirectory corresponding to the version in AndroidManifest.xml.
pari-config: $(PARI_SRC)
	rm -rf $(PARI_SRC)/$(TARGET)
	cd $(PARI_SRC) && \
	sed "s?XXXCCXXX?$(CC)?" $(TOP)/config/$(PARI_VERSION)/pari.cfg > pari.cfg && \
	./Configure -l ./pari.cfg

pari-make:
	cd $(PARI_SRC)/$(TARGET) && \
	make -j $(shell getconf _NPROCESSORS_CONF) all && \
	make install-include install-lib-dyn LIBPARI_SONAME=libpari.so DESTDIR=$(JNI)/pari

pari-check:
	cd $(PARI_SRC)/$(TARGET) && \
	adb push gp-sta /data && \
	make statest-all RUNTEST=$(TOP)/runtest.bench && \
	adb shell rm /data/gp-sta /data/pari-ret /data/pari-stdin


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


# Use the pari version specified in AndroidManifest.xml.
check: pari pari-check

# Use the pari version that happens to be in the source tree; useful for
# manual checking of pari versions independently of the Android manifest.
check-standalone: pari-standalone pari-check

clean:
	rm -f PariDroid*.apk
	cd $(PARIDROID_SRC) && ant clean
	cd $(JNI) && $(NDK)/ndk-build clean V=1
	rm -rf $(JNI)/pari

distclean: clean
	rm -rf $(PARI_SRC)/$(TARGET)
	rm -f $(PARI_SRC)/config-arm-linux*
	rm -f $(PARI_SRC)/config/linux-arm*


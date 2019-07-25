# This Makefile uses the environment variable $(NDK), which needs to
# contain the path to the Android NDK. Please set it in your environment
# before invoking make.

.PHONY: all version apk apk-fast pari pari-standalone pari-checkout pari-config-standalone pari-make pari-make-arm pari-make-aarch64 pari-check check check-standalone clean distclean

# Specify the GNU triplets for the cross compiler with the notation of
# https://www.gnu.org/savannah-checkouts/gnu/autoconf/manual/autoconf-2.69/html_node/System-Type.html#System-Type
TARGET_CPU_ARM=arm
TARGET_CPU_AARCH64=aarch64
TARGET_KERNEL=linux
TARGET_OS_ARM=androideabi
TARGET_OS_AARCH64=android
TARGET_SYSTEM_ARM=$(TARGET_KERNEL)-$(TARGET_OS_ARM)
TARGET_SYSTEM_AARCH64=$(TARGET_KERNEL)-$(TARGET_OS_AARCH64)
TARGET_ARM=$(TARGET_CPU_ARM)-$(TARGET_SYSTEM_ARM)
TARGET_AARCH64=$(TARGET_CPU_AARCH64)-$(TARGET_SYSTEM_AARCH64)

# Settings for the cross compiler
NDK_PLATFORM=android-24
GCC_VERSION=4.9
SYSROOT_ARM=$(NDK)/platforms/$(NDK_PLATFORM)/arch-arm
SYSROOT_AARCH64=$(NDK)/platforms/$(NDK_PLATFORM)/arch-arm64
CC_ARM='$(NDK)/toolchains/$(TARGET_ARM)-$(GCC_VERSION)/prebuilt/linux-x86_64/bin/$(TARGET_ARM)-gcc --sysroot=$(SYSROOT_ARM)'
CC_AARCH64='$(NDK)/toolchains/$(TARGET_AARCH64)-$(GCC_VERSION)/prebuilt/linux-x86_64/bin/$(TARGET_AARCH64)-gcc --sysroot=$(SYSROOT_AARCH64)'

# Settings for the PARI and Android target CPUs
PARI_TARGET_ARM=Oandroid-$(TARGET_CPU_ARM)
PARI_TARGET_AARCH64=Oandroid-$(TARGET_CPU_AARCH64)
ANDROID_TARGET_ARM=armeabi-v7a
ANDROID_TARGET_AARCH64=arm64-v8a

PARIDROID_VERSION=$(shell grep versionName PariDroid/AndroidManifest.xml | grep -o '".*"' | tr -d '"')
PARI_VERSION=$(shell echo $(PARIDROID_VERSION) | cut -d. -f1-3)
ifeq "$(PARI_VERSION)" "2.12.0"
   PARI_GIT=origin/master
else
   PARI_GIT=pari-$(PARI_VERSION)
endif

TOP=$(CURDIR)
PARIDROID_SRC=$(TOP)/PariDroid
PARI_SRC=$(TOP)/../srclib/pari
JNI=$(PARIDROID_SRC)/jni

all: apk

# Check whether the $(NDK) environment variable is defined and emit a warning
# otherwise.
# Print some version information for debugging and logging purposes.
version:
	@if test -z "$$NDK"; then echo 'THE ENVIRONMENT VARIABLE $$NDK MUST BE DEFINED!'; fi
	@echo Compiler: $(CC)
	@echo PARIDROID_VERSION: $(PARIDROID_VERSION)
	@echo PARI_VERSION: $(PARI_VERSION)

apk: pari apk-fast

apk-fast: $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h
	cd $(PARIDROID_SRC) && $(NDK)/ndk-build V=1 NDK_TOOLCHAIN_VERSION=4.9 APP_ABI="$(ANDROID_TARGET_ARM) $(ANDROID_TARGET_AARCH64)"
	cd $(PARIDROID_SRC) && ant release
	cp $(PARIDROID_SRC)/bin/PariDroid-release-unsigned.apk $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $(HOME)/.android/debug.keystore -storepass android $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk androiddebugkey
	zipalign -f 4 $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk PariDroid-$(PARIDROID_VERSION).apk

# Use the pari version specified in AndroidManifest.xml.
pari: version pari-checkout pari-make

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
pari-config-standalone: version
	rm -rf $(PARI_SRC)/$(PARI_TARGET)
	cd $(PARI_SRC) && \
	CC=$(CC) RUNTEST=$(TOP)/runtest ./Configure --host=$(TARGET_CPU)-linux --builddir=$(PARI_TARGET) --kernel=$(TARGET_CPU)-none --without-readline --graphic=none --prefix=/ --datadir=/data/data/fr.ubordeaux.math.paridroid && \
	sed -i 's/^TOP=.*/TOP=../' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^RUNTEST=.*/RUNTEST=/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^CC=.*/CC=XXXCCXXX/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^LD=.*/LD=XXXCCXXX/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^LDused=.*/LDused=/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^DLLD=.*/DLLD=XXXCCXXX/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^MODLD=.*/MODLD=/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^modules_build=.*/modules_build=/' $(PARI_TARGET)/pari.cfg && \
	sed -i 's/^prefix=.*/prefix=/' $(PARI_TARGET)/pari.cfg
#	sed -i "s/^LDFLAGS='-O3\ -Wall/LDFLAGS='-O3/" $(PARI_TARGET)/pari.cfg

# Configure pari using the pari.cfg taken from the appropriate config/
# subdirectory corresponding to the version in AndroidManifest.xml.
pari-make: pari-make-arm pari-make-aarch64

pari-make-arm:
	rm -rf $(PARI_SRC)/$(PARI_TARGET_ARM)
	cd $(PARI_SRC) && \
	sed "s?XXXCCXXX?$(CC_ARM)?" $(TOP)/config/$(PARI_VERSION)/$(TARGET_CPU_ARM)/pari.cfg > pari.cfg && \
	./Configure -l ./pari.cfg
	cd $(PARI_SRC)/$(PARI_TARGET_ARM) && \
	make -j $(shell getconf _NPROCESSORS_CONF) all && \
	make install-include install-lib-dyn LIBPARI_SONAME=libpari.so DESTDIR=$(JNI)/$(ANDROID_TARGET_ARM)/pari

pari-make-aarch64:
	rm -rf $(PARI_SRC)/$(PARI_TARGET_AARCH64)
	cd $(PARI_SRC) && \
	sed "s?XXXCCXXX?$(CC_AARCH64)?" $(TOP)/config/$(PARI_VERSION)/$(TARGET_CPU_AARCH64)/pari.cfg > pari.cfg && \
	./Configure -l ./pari.cfg
	cd $(PARI_SRC)/$(PARI_TARGET_AARCH64) && \
	make -j $(shell getconf _NPROCESSORS_CONF) all && \
	make install-include install-lib-dyn LIBPARI_SONAME=libpari.so DESTDIR=$(JNI)/$(ANDROID_TARGET_AARCH64)/pari

pari-check: pari-check-arm pari-check-aarch64

pari-check-arm: 
	cd $(PARI_SRC)/$(PARI_TARGET_ARM) && \
	adb root \
	adb push gp-sta /data && \
	make statest-all RUNTEST=$(TOP)/runtest.bench && \
	adb shell rm /data/gp-sta /data/pari-ret /data/pari-stdin

pari-check-aarch64:
	cd $(PARI_SRC)/$(PARI_TARGET_AARCH64) && \
	adb root \
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
	cd $(JNI) && $(NDK)/ndk-build clean V=1 APP_ABI="$(ANDROID_TARGET_ARM) $(ANDROID_TARGET_AARCH64)"
	rm -rf $(JNI)/$(ANDROID_TARGET_ARM)
	rm -rf $(JNI)/$(ANDROID_TARGET_AARCH64)

distclean: clean
	rm -rf $(PARI_SRC)/$(PARI_TARGET_ARM)
	rm -rf $(PARI_SRC)/$(PARI_TARGET_AARCH64)
	rm -f $(PARI_SRC)/config-$(TARGET_CPU_ARM)-linux*
	rm -f $(PARI_SRC)/config-$(TARGET_CPU_AARCH64)-linux*


.PHONY: all version apk apk-fast ndk gmp-build-generic gmp-build-arm gmp-build-aarch64 gmp-build-x86 gmp-build-x86-64 pari pari-checkout pari-build pari-build-arm pari-build-aarch64 pari-build-x86 pari-build-x86-64 pari-configure-arm pari-configure-aarch64 pari-configure-x86 pari-configure-x86-64 pari-make-generic pari-make-arm pari-make-aarch64 pari-make-x86 pari-make-x86-64 pari-make-arm pari-make-aarch64 pari-make-x86 pari-make-x86-64 check pari-check pari-check-arm pari-check-aarch64 pari-check-x86 clean distclean

# Specify the GNU triplets for the cross compiler with the notation of
# https://www.gnu.org/savannah-checkouts/gnu/autoconf/manual/autoconf-2.69/html_node/System-Type.html#System-Type
TARGET_CPU_ARM=arm
TARGET_CPU_AARCH64=aarch64
TARGET_CPU_X86=x86
TARGET_CPU_X86_64=x86_64
TARGET_KERNEL=linux
TARGET_OS=android
TARGET_SYSTEM=$(TARGET_KERNEL)-$(TARGET_OS)
TARGET_ARM=$(TARGET_CPU_ARM)-$(TARGET_SYSTEM)
TARGET_AARCH64=$(TARGET_CPU_AARCH64)-$(TARGET_SYSTEM)
TARGET_X86=$(TARGET_CPU_X86)-$(TARGET_SYSTEM)
TARGET_X86_64=$(TARGET_CPU_X86_64)-$(TARGET_SYSTEM)

# Settings for the cross compiler.
NDK?=/usr/local/android-ndk-r13b
# Compile the arm and aarch64 targets with different versions of
# NDK_PLATFORM: aarch64 appears in android-21; to be able to run on older
# Android versions, we compile the arm target with earlier tools.
NDK_PLATFORM_ARM=android-9
NDK_PLATFORM_AARCH64=android-21
NDK_PLATFORM_X86=android-9
NDK_PLATFORM_X86_64=android-21
SYSROOT_ARM=$(NDK)/platforms/$(NDK_PLATFORM_ARM)/arch-arm
SYSROOT_AARCH64=$(NDK)/platforms/$(NDK_PLATFORM_AARCH64)/arch-arm64
SYSROOT_X86=$(NDK)/platforms/$(NDK_PLATFORM_X86)/arch-x86
SYSROOT_X86_64=$(NDK)/platforms/$(NDK_PLATFORM_X86_64)/arch-x86_64
CC_ARM=$(NDK)/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc
CC_AARCH64=$(NDK)/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64/bin/aarch64-linux-android-gcc
CC_X86=$(NDK)/toolchains/x86-4.9/prebuilt/linux-x86_64/bin/i686-linux-android-gcc
CC_X86_64=$(NDK)/toolchains/x86_64-4.9/prebuilt/linux-x86_64/bin/x86_64-linux-android-gcc

# Settings for the GMP, PARI and Android target CPUs
PARI_TARGET_ARM=Olinux-$(TARGET_CPU_ARM)
PARI_TARGET_AARCH64=Olinux-$(TARGET_CPU_AARCH64)
PARI_TARGET_X86=Olinux-$(TARGET_CPU_X86)
PARI_TARGET_X86_64=Olinux-$(TARGET_CPU_X86_64)
ANDROID_TARGET_ARM=armeabi
ANDROID_TARGET_AARCH64=arm64-v8a
ANDROID_TARGET_X86=x86
ANDROID_TARGET_X86_64=x86_64

# Choose what to put into the apk.
# The emulator can use x86 targets.
APP_ABI="$(ANDROID_TARGET_ARM) $(ANDROID_TARGET_AARCH64) $(ANDROID_TARGET_X86) $(ANDROID_TARGET_X86_64)"
# For the release, ARM architectures are enough.
# APP_ABI="$(ANDROID_TARGET_ARM) $(ANDROID_TARGET_AARCH64)"

PARIDROID_VERSION=$(shell grep versionName PariDroid/AndroidManifest.xml | grep -o '".*"' | tr -d '"')
PARI_VERSION=$(shell echo $(PARIDROID_VERSION) | cut -d. -f1-3)
ifeq "$(PARI_VERSION)" "2.12.0"
   PARI_GIT=origin/master
else
   PARI_GIT=pari-$(PARI_VERSION)
endif
GMP_VERSION=6.2.0

TOP=$(CURDIR)
PARIDROID_SRC=$(TOP)/PariDroid
PARI_SRC=$(TOP)/../srclib/pari
GMP_SRC=$(TOP)/../srclib/gmp
JNI=$(PARIDROID_SRC)/jni

all: apk

# Print some version information for debugging and logging purposes.
version:
	@echo Compiler $(ANDROID_TARGET_ARM): $(CC_ARM)
	@echo Compiler $(ANDROID_TARGET_AARCH64): $(CC_AARCH64)
	@echo Compiler $(ANDROID_TARGET_X86): $(CC_X86)
	@echo Compiler $(ANDROID_TARGET_X86_64): $(CC_X86_64)
	@echo PARIDROID_VERSION: $(PARIDROID_VERSION)
	@echo PARI_VERSION: $(PARI_VERSION)

apk: pari apk-fast

ndk: $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h
	cd $(PARIDROID_SRC) && $(NDK)/ndk-build V=1 NDK_TOOLCHAIN_VERSION=4.9 APP_ABI=$(APP_ABI)

apk-fast: ndk
	cd $(PARIDROID_SRC) && ant release
	cp $(PARIDROID_SRC)/bin/PariDroid-release-unsigned.apk $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $(HOME)/.android/debug.keystore -storepass android $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk androiddebugkey
	zipalign -f 4 $(PARIDROID_SRC)/bin/PariDroid-release-unaligned.apk PariDroid-$(PARIDROID_VERSION).apk

gmp-build-generic: $(GMP_SRC)
	- cd $(GMP_SRC) &&  make distclean
	cd $(GMP_SRC) && \
	CC=$(CC) CFLAGS="--sysroot=$(SYSROOT) --std=c99" CPPFLAGS=--sysroot=$(SYSROOT) LDFLAGS=--sysroot=$(SYSROOT) ./configure --enable-maintainer-mode --prefix=$(JNI)/$(ANDROID_TARGET)/gmp --host=$(TARGET) --with-sysroot=$(SYSROOT) && \
	make -j $(shell getconf _NPROCESSORS_CONF) && \
	make install

gmp-build-arm:
	make CC=$(CC_ARM) SYSROOT=$(SYSROOT_ARM) ANDROID_TARGET=$(ANDROID_TARGET_ARM) TARGET=$(TARGET_ARM) gmp-build-generic

gmp-build-aarch64:
	make CC=$(CC_AARCH64) SYSROOT=$(SYSROOT_AARCH64) ANDROID_TARGET=$(ANDROID_TARGET_AARCH64) TARGET=$(TARGET_AARCH64) gmp-build-generic

gmp-build-x86:
	make CC=$(CC_X86) SYSROOT=$(SYSROOT_X86) ANDROID_TARGET=$(ANDROID_TARGET_X86) TARGET=$(TARGET_X86) gmp-build-generic

gmp-build-x86-64:
	make CC=$(CC_X86_64) SYSROOT=$(SYSROOT_X86_64) ANDROID_TARGET=$(ANDROID_TARGET_X86_64) TARGET=$(TARGET_X86_64) gmp-build-generic

# Use the pari version specified in AndroidManifest.xml.
pari: version pari-checkout pari-build

# Updates the pari source tree and switches to the version from
# AndroidManifest.xml
pari-checkout: $(PARI_SRC)
	cd $(PARI_SRC) && git fetch && git checkout $(PARI_GIT)

pari-build: pari-build-arm pari-build-aarch64 pari-build-x86 pari-build-x86-64

pari-build-arm: gmp-build-arm pari-configure-arm pari-make-arm

pari-build-aarch64: gmp-build-aarch64 pari-configure-aarch64 pari-make-aarch64

pari-build-x86: gmp-build-x86 pari-configure-x86 pari-make-x86

pari-build-x86-64: gmp-build-x86-64 pari-configure-x86-64 pari-make-x86-64

pari-configure-arm:
	# First use the PARI mechanism for configuration, but not executing
	# any compiled code in a cross-compilation context.
	rm -rf $(PARI_SRC)/$(PARI_TARGET_ARM)
	cd $(PARI_SRC) && \
	CC=$(CC_ARM) CFLAGS=--sysroot=$(SYSROOT_ARM) RUNTEST="echo $(GMP_VERSION)" ./Configure --prefix=$(JNI)/$(ANDROID_TARGET_ARM)/pari --datadir=/data/data/fr.ubordeaux.math.paridroid --with-gmp=$(JNI)/$(ANDROID_TARGET_ARM)/gmp --host=$(TARGET_CPU_ARM)-linux
	# Correct the resulting pari.cfg.
	cd $(PARI_SRC)/$(PARI_TARGET_ARM) && \
	sed -i -E "s/^doubleformat='(.*)'/doubleformat='1'/" pari.cfg
	# Finally run Configure again with the new pari.cfg to create other
	# output files such as paricfg.h.
	cd $(PARI_SRC) && \
	./Configure -l $(PARI_TARGET_ARM)/pari.cfg

pari-configure-aarch64:
	rm -rf $(PARI_SRC)/$(PARI_TARGET_AARCH64)
	cd $(PARI_SRC) && \
	CC=$(CC_AARCH64) CFLAGS=--sysroot=$(SYSROOT_AARCH64) RUNTEST="echo $(GMP_VERSION)" ./Configure --prefix=$(JNI)/$(ANDROID_TARGET_AARCH64)/pari --datadir=/data/data/fr.ubordeaux.math.paridroid --with-gmp=$(JNI)/$(ANDROID_TARGET_AARCH)/gmp --host=$(TARGET_CPU_AARCH64)-linux
	cd $(PARI_SRC)/$(PARI_TARGET_AARCH64) && \
	sed -i "s!^asmarch='none'!asmarch='aarch64'!" pari.cfg && \
	sed -i "s!^kernlvl0='none'!kernlvl0='aarch64'!" pari.cfg && \
	sed -i "s!^doubleformat=''!doubleformat='-'!" pari.cfg && \
	sed -i "s!^sizeof_long='4'!sizeof_long='8'!" pari.cfg && \
	sed -i "s!^kernlvl1='none'!kernlvl1='gmp'!" pari.cfg && \
	sed -i "s!(portable C kernel) 32-bit!(aarch64/GMP-$(GMP_VERSION) kernel) 64-bit!" pari.cfg && \
	sed -i "s!^libpari_base='pari'!libpari_base='pari-gmp'!" pari.cfg && \
	sed -i "s!^gmp=''!gmp='$(JNI)/$(ANDROID_TARGET_AARCH)/gmp/lib'!" pari.cfg && \
	sed -i "s!^GMPLIBS=''!GMPLIBS='-L$(JNI)/$(ANDROID_TARGET_AARCH64)/gmp/lib -lgmp'!" pari.cfg && \
	sed -i "s!^GMPINCLUDE=''!GMPINCLUDE='-I$(JNI)/$(ANDROID_TARGET_AARCH64)/gmp/include'!" pari.cfg
	cd $(PARI_SRC) && \
	./Configure -l $(PARI_TARGET_AARCH64)/pari.cfg

pari-configure-x86:
	rm -rf $(PARI_SRC)/$(PARI_TARGET_X86)
	cd $(PARI_SRC) && \
	CC=$(CC_X86) CFLAGS=--sysroot=$(SYSROOT_X86) RUNTEST="echo $(GMP_VERSION)" ./Configure --prefix=$(JNI)/$(ANDROID_TARGET_X86)/pari --datadir=/data/data/fr.ubordeaux.math.paridroid --with-gmp=$(JNI)/$(ANDROID_TARGET_X86)/gmp --host=$(TARGET_CPU_X86)-linux
	cd $(PARI_SRC)/$(PARI_TARGET_X86) && \
	sed -i "s!^asmarch='none'!asmarch='ix86'!" pari.cfg && \
	sed -i "s!^kernlvl0='none'!kernlvl0='ix86'!" pari.cfg && \
	sed -i -E "s!portable C/(.*) kernel!x86/GMP-$(GMP_VERSION) kernel!" pari.cfg && \
	sed -i -E "s!^doubleformat='(.*)'!doubleformat='1'!" pari.cfg
	cd $(PARI_SRC) && \
	./Configure -l $(PARI_TARGET_X86)/pari.cfg

pari-configure-x86-64:
	rm -rf $(PARI_SRC)/$(PARI_TARGET_X86_64)
	cd $(PARI_SRC) && \
	CC=$(CC_X86_64) CFLAGS=--sysroot=$(SYSROOT_X86_64) RUNTEST="echo $(GMP_VERSION)" ./Configure --prefix=$(JNI)/$(ANDROID_TARGET_X86_64)/pari --datadir=/data/data/fr.ubordeaux.math.paridroid --with-gmp=$(JNI)/$(ANDROID_TARGET_X86_64)/gmp --host=$(TARGET_CPU_X86_64)-linux
	cd $(PARI_SRC)/$(PARI_TARGET_X86_64) && \
	sed -i "s!^asmarch='ix86'!asmarch='x86_64'!" pari.cfg && \
	sed -i "s!^kernlvl0='ix86'!kernlvl0='x86_64'!" pari.cfg && \
	sed -i -E "s!\(ix86/(.*) kernel\) 32-bit!(x86-64/GMP-$(GMP_VERSION)) kernel) 64-bit!" pari.cfg && \
	sed -i -E "s!^doubleformat='(.*)'!doubleformat='-'!" pari.cfg && \
	sed -i "s!^sizeof_long='4'!sizeof_long='8'!" pari.cfg
	cd $(PARI_SRC) && \
	./Configure -l $(PARI_TARGET_X86_64)/pari.cfg

pari-make-generic:
	cd $(PARI_SRC)/$(PARI_TARGET) && \
	make -j $(shell getconf _NPROCESSORS_CONF) lib-dyn && \
	make install-include install-lib-dyn

pari-make-arm:
	make PARI_TARGET=$(PARI_TARGET_ARM) pari-make-generic

pari-make-aarch64:
	make PARI_TARGET=$(PARI_TARGET_AARCH64) pari-make-generic

pari-make-x86:
	make PARI_TARGET=$(PARI_TARGET_X86) pari-make-generic

pari-make-x86-64:
	make PARI_TARGET=$(PARI_TARGET_X86_64) pari-make-generic

pari-check: pari-check-arm pari-check-aarch64

pari-check-arm: 
	cd $(PARI_SRC)/$(PARI_TARGET_ARM) && \
	make statest-all

pari-check-aarch64:
	cd $(PARI_SRC)/$(PARI_TARGET_AARCH64) && \
	make statest-all 

pari-check-x86: 
	cd $(PARI_SRC)/$(PARI_TARGET_X86) && \
	adb root && \
	adb push gp-sta /data && \
	make statest-all RUNTEST=$(TOP)/runtest.bench && \
	adb shell rm /data/gp-sta /data/pari-ret /data/pari-stdin

# These are supposed to be executed only once to set up the repositories.
$(PARI_SRC):
	mkdir -p $(PARI_SRC)
	git clone http://pari.math.u-bordeaux.fr/git/pari.git $(PARI_SRC)

$(GMP_SRC):
	mkdir -p $(GMP_SRC)
	hg clone https://gmplib.org/repo/gmp-6.2/ $(GMP_SRC)
	cd $(GMP_SRC) && ./.bootstrap

# The following targets create files that ultimately are supposed to be under
# version control, but that can be generated automatically.

# Create a C header file from the java class interfacing with the pari
# library; this gives the prototypes of functions to be written by hand in
# the corresponding C source file. The result is under version control.
$(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h: $(PARIDROID_SRC)/src/fr/ubordeaux/math/paridroid/PariNative.java
	cd $(PARIDROID_SRC) && ant debug
	cd $(PARIDROID_SRC)/bin/classes && javah -jni fr.ubordeaux.math.paridroid.PariNative
	mv $(PARIDROID_SRC)/bin/classes/fr_ubordeaux_math_paridroid_PariNative.h $(JNI)/paridroid/fr_ubordeaux_math_paridroid_PariNative.h


check: pari pari-check

clean:
	rm -f PariDroid*.apk
	cd $(PARIDROID_SRC) && ant clean
	cd $(JNI) && $(NDK)/ndk-build clean V=1 APP_ABI="$(ANDROID_TARGET_ARM) $(ANDROID_TARGET_AARCH64) $(ANDROID_TARGET_X86) $(ANDROID_TARGET_X86_64)"
	rm -rf $(JNI)/$(ANDROID_TARGET_ARM)
	rm -rf $(JNI)/$(ANDROID_TARGET_AARCH64)
	rm -rf $(JNI)/$(ANDROID_TARGET_X86)
	rm -rf $(JNI)/$(ANDROID_TARGET_X86_64)

distclean: clean
	rm -rf $(PARI_SRC)/$(PARI_TARGET_ARM)
	rm -rf $(PARI_SRC)/$(PARI_TARGET_AARCH64)
	rm -rf $(PARI_SRC)/$(PARI_TARGET_X86)
	rm -rf $(PARI_SRC)/$(PARI_TARGET_X86_64)
	rm -f $(PARI_SRC)/config-$(TARGET_CPU_ARM)-linux*
	rm -f $(PARI_SRC)/config-$(TARGET_CPU_AARCH64)-linux*
	rm -f $(PARI_SRC)/config-$(TARGET_CPU_X86)-linux*
	rm -f $(PARI_SRC)/config-$(TARGET_CPU_X86_64)-linux*


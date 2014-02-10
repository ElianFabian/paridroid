.PHONY: all apk paridroid libpari clean ndk-clean pari-clean uberclean

include ./Makefile.include
CC=$(TOOLCHAIN)/bin/arm-linux-androideabi-gcc

PARI_SRC=$(TOP)/pari
PARIDROID_SRC=$(TOP)/PariDroid
NDK_MODULES=$(PARIDROID_SRC)/jni
PARI_PREFIX=$(NDK_MODULES)/pari

all: libpari paridroid apk

apk: paridroid
	cp $(PARIDROID_SRC)/bin/PariDroid-debug.apk $(TOP)/PariDroid.apk

paridroid: $(NDK_MODULES)/paridroid/org_freemonad_paridroid_PariNative.h
	cd $(NDK_MODULES) && $(NDK_PATH)/ndk-build V=1
	cd $(PARIDROID_SRC) && ant debug

$(NDK_MODULES)/paridroid/org_freemonad_paridroid_PariNative.h: libpari
	cd $(PARIDROID_SRC) && ant debug
	cd $(PARIDROID_SRC)/bin/classes && javah -jni org.freemonad.paridroid.PariNative
	mv $(PARIDROID_SRC)/bin/classes/org_freemonad_paridroid_PariNative.h $(NDK_MODULES)/paridroid/org_freemonad_paridroid_PariNative.h

libpari:
	cd $(PARI_SRC) && \
	CC=$(CC) RUNTEST="../runtest" ./Configure --host=arm-linux --builddir=Oandroid-arm --kernel=arm-none --without-readline --prefix=$(PARI_PREFIX) && \
	./Configure -l Oandroid-arm && \
	cd Oandroid-arm && \
	$(MAKE) all && \
	make install-include install-lib-dyn LIBPARI_SONAME=libpari.so

clean:
	rm -f $(TOP)/PariDroid.apk
	cd $(PARIDROID_SRC) && ant clean

distclean: clean
	cd $(NDK_MODULES) && $(NDK_PATH)/ndk-build clean V=1
	rm -rf $(PARI_PREFIX)
	rm -rf $(PARI_SRC)/android
	rm -rf $(PARI_SRC)/Oandroid-arm

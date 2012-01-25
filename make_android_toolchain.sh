#!/bin/bash

echo "Be sure to set NDK, NDK_VERSION and TARGET_PLATFORM variables to the right directory!"

NDK_VERSION=r7
NDK=~/opt/android/android-ndk-$NDK_VERSION

TARGET_PLATFORM=android-8
TOOLCHAIN_INSTALL_DIR=/tmp/android-toolchain

$NDK/build/tools/make-standalone-toolchain.sh --platform=$TARGET_PLATFORM --install-dir=$TOOLCHAIN_INSTALL_DIR

echo "Unpacked Android NDK toolchain (target=android-$TARGET_PLATFORM) under $TOOLCHAIN_INSTALL_DIR."

cp $TOOLCHAIN_INSTALL_DIR/sysroot/usr/include/termios.h $TOOLCHAIN_INSTALL_DIR/sysroot/usr/include/sys/termios.h
#EOF